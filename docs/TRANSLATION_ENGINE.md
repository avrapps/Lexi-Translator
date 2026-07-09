# Translation Engine — Technical Documentation

## Overview

Lexi's translation engine performs fully offline neural machine translation using OPUS-MT models via ONNX Runtime. The pipeline runs entirely on-device with no cloud dependencies.

## Architecture

```
Input Text
    │
    ▼
┌─────────────────────┐
│  Sentence Splitter   │  Split on .!? boundaries, preserve paragraph structure
└──────────┬──────────┘
           │ (per sentence)
           ▼
┌─────────────────────┐
│  SentencePiece       │  Unigram tokenizer with Viterbi segmentation
│  Tokenizer           │  Loads from tokenizer.json (58K vocab, log-prob scores)
└──────────┬──────────┘
           │ token IDs
           ▼
┌─────────────────────┐
│  ONNX Encoder        │  encoder_model_quantized.onnx (OPUS-MT Marian encoder)
│                       │  Produces encoder hidden states
└──────────┬──────────┘
           │ hidden states
           ▼
┌─────────────────────┐
│  ONNX Decoder        │  decoder_model_quantized.onnx (autoregressive generation)
│  (Greedy + Penalty)  │  Greedy decoding with repetition penalty (1.3x, window=6)
└──────────┬──────────┘
           │ output token IDs
           ▼
┌─────────────────────┐
│  Detokenizer         │  Maps IDs back to text, removes ▁ markers
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Post-processing     │  Punctuation cleanup, paragraph assembly
└─────────────────────┘
           │
           ▼
      Output Text
```

## Key Design Decisions

### 1. SentencePiece Unigram Tokenizer (NOT BPE)

OPUS-MT models from Helsinki-NLP use **SentencePiece Unigram** tokenization, not BPE. This was discovered during implementation — the Hugging Face `tokenizer.json` declares `"type": "Unigram"` with a vocabulary of `[(token, log_probability), ...]` pairs.

**Implementation**: Viterbi algorithm that finds the segmentation maximizing the sum of log-probabilities across all subword tokens.

**Key insight**: The naive approach of word-level dictionary lookup produced catastrophically bad translations ("Denglish" — mixed English/German with corrupted words). Only proper Unigram segmentation produces correct token IDs that the decoder can handle.

### 2. Sentence-by-Sentence Translation

Long texts (multiple sentences) are translated one sentence at a time rather than as a single input because:

- OPUS-MT has an effective context window of ~512 tokens
- The greedy decoder without KV-cache degrades rapidly on long sequences
- Sentence-level translation prevents cross-sentence repetition loops
- Enables streaming UI updates (user sees results appear progressively)

**Splitting strategy**: `(?<=[.!?])\s+` regex, with paragraph boundaries preserved via `\n` splitting first.

### 3. Greedy Decoding with Repetition Penalty

We use greedy decoding (argmax at each step) rather than beam search because:

- Beam search requires KV-cache management which the non-merged decoder ONNX model doesn't support efficiently
- Greedy + repetition penalty produces acceptable quality for OPUS-MT models
- Significantly simpler implementation and faster inference

**Repetition penalty**: Factor of 1.3x applied to the last 6 generated tokens. If a token's logit is positive, divide by 1.3; if negative, multiply by 1.3. This prevents the decoder from getting stuck in loops like "in den Mitgliedstaaten der Europäischen Union in den Mitgliedstaaten..."

**Max generation length**: Capped at 200 tokens per sentence to prevent runaway generation.

### 4. Non-Merged Decoder Model

We use `decoder_model_quantized.onnx` (non-merged) instead of `decoder_model_merged_quantized.onnx` because:

- The merged model requires a `use_cache_branch` boolean input for KV-cache toggling (Optimum/transformers.js format)
- Without proper KV-cache management, the merged model throws ONNX Runtime errors
- The non-merged decoder takes simple inputs: `input_ids`, `encoder_hidden_states`, `encoder_attention_mask`
- Trade-off: slightly slower (recomputes full sequence at each step) but works reliably

### 5. Post-Processing Cleanup

Sentence-by-sentence translation can produce punctuation artifacts at join boundaries. A `cleanPunctuation()` function handles:

- Duplicate punctuation with spaces: `". ."` → `"."`
- Space before punctuation: `"wahr? ."` → `"wahr?"`
- Double marks (but preserves ellipsis)
- Missing space after sentence-end before uppercase
- Collapsing multiple newlines to single newline
- Preserving paragraph structure from input

## Model Files Required (per language pair)

Each translation model requires these files in `~/.lexi/models/{model-id}/`:

| File | Size | Purpose |
|------|------|---------|
| `encoder_model_quantized.onnx` | ~32-50 MB | Encodes source text to hidden states |
| `decoder_model_quantized.onnx` | ~50-60 MB | Generates target tokens autoregressively |
| `tokenizer.json` | ~5.5 MB | SentencePiece Unigram vocab + scores |
| `vocab.json` | ~1.4 MB | Token-to-ID mapping (fallback) |

Total per language pair: ~90-120 MB

## Platform Implementation

### JVM (Desktop)

- `OnnxTranslationSession` — Manages ONNX Runtime encoder/decoder sessions
- `MarianTokenizer` — SentencePiece Unigram tokenizer with Viterbi algorithm
- `JvmTranslationInferenceProvider` — Orchestrates tokenize→encode→decode→detokenize
- Supplementary files (decoder, tokenizer.json) are lazily downloaded on first translation if missing

### Android

- Same ONNX Runtime inference (via `onnxruntime-android`)
- Uses NNAPI execution provider for hardware acceleration on supported devices
- Model files stored in app's internal storage (`files/models/`)

## Known Limitations

1. **No beam search** — Greedy decoding can miss globally optimal translations
2. **No KV-cache** — Each decoder step reprocesses the full generated sequence (O(n²) per sentence)
3. **512 token input limit** — Very long sentences (>~100 words) may be truncated
4. **No language detection model** — Uses heuristic character-set detection as placeholder
5. **Single-direction models** — Each language pair requires its own model (en→de is separate from de→en)

## Future Improvements

- Implement beam search (num_beams=4) with merged decoder + KV-cache
- Add proper language identification model (fastText or similar)
- Support bidirectional models or model chaining (en→de + de→en in same download)
- Quantize to int4 for smaller model sizes on mobile
- Add dictionary lookup integration for short phrases
