import SwiftUI
import SharedLogic

struct ContentView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("Hello World!")
                .font(.largeTitle)
                .fontWeight(.bold)
            Text("Welcome to AITranslator")
                .font(.title2)
                .foregroundColor(.secondary)
            Text("iOS: \(Greeting().greet())")
                .font(.body)
                .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}