# Swift API Calling Guide

## **Objective**

Learn how to:

* Call APIs using `async/await`
* Understand when to use `Codable` (JSON)
* Understand when to use direct URLs (images/files)
* Display API data in UI (Text and Image)
* Handle nested JSON responses

---

## **1. Two Types of APIs**

Always open the API in a browser before coding.

### **JSON API**

**Example:**

```json
{ "quote": "Hello world" }
```

You need to:

* Create a `struct`
* Decode using `Codable`

---

### **Direct Content API**

**Example:**

```
https://robohash.org/hello
```

You will directly get an image or file.

You do not need:

* Struct
* Decoding

You just use the URL.

---

## **2. Basic Flow**

For JSON APIs:

1. Create URL
2. Fetch data
3. Decode JSON
4. Use result

---

## **3. Model (Struct)**

Match your struct to the JSON:

```swift
struct Model: Codable {
    let key: String
}
```

---

## **4. Fetching Data (Understanding Each Step)**

### **Step A: Create URL**

```swift
let url = URL(string: "YOUR_API_URL")
```

This is your API endpoint.

---

### **Step B: Safe Check**

```swift
guard let url = URL(string: "YOUR_API_URL") else {
    return nil
}
```

Ensures URL is valid.

---

### **Step C: Fetch Data**

```swift
let (data, _) = try await URLSession.shared.data(from: url)
```

* Sends request
* Waits for response
* Returns data

---

### **Step D: Decode JSON**

```swift
let result = try JSONDecoder().decode(Model.self, from: data)
```

* Converts JSON → Swift object

---

### **Step E: Error Handling**

```swift
do {
    // network + decoding
} catch {
    print("Error:", error)
}
```

---

## **5. Full Function**

```swift
func fetchData() async -> Model? {
    guard let url = URL(string: "YOUR_API_URL") else {
        return nil
    }

    do {
        let (data, _) = try await URLSession.shared.data(from: url)
        let result = try JSONDecoder().decode(Model.self, from: data)
        return result
    } catch {
        print("Error:", error)
        return nil
    }
}
```

---

## **6. Array vs Object**

* `{}` → `Model.self`
* `[]` → `[Model].self`

---

## **7. Nested JSON**

**Example:**

```json
{
  "results": [
    {
      "name": "Rick",
      "image": "https://example.com/image.png"
    }
  ]
}
```

### **How to Think**

* Outer object → main struct
* Array → use `[]`
* Inner object → separate struct

### **Structs**

```swift
struct APIResponse: Codable {
    let results: [Character]
}

struct Character: Codable {
    let name: String
    let image: String
}
```

### **Decode**

```swift
let result = try JSONDecoder().decode(APIResponse.self, from: data)
```

### **Access Data**

```swift
result.results.first?.name
result.results.first?.image
```

**Explanation:**

* `result` → full response
* `results` → array
* `first` → first item
* `?.name` → safe access

---

## **8. Using API in UI**

Keep API logic separate from UI.

---

## **9. Storing Data**

```swift
@State var textData: String = ""
@State var imageURL: String = ""
```

---

## **10. Showing Text**

```swift
Text(textData)
```

---

## **11. Showing Image**

```swift
if let url = URL(string: imageURL) {
    AsyncImage(url: url)
}
```

---

## **12. Direct Image API**

```swift
AsyncImage(url: URL(string: "https://example.com/image"))
```

---

## **13. Final Flow**

1. Understand API
2. Create struct
3. Fetch data
4. Store values
5. Show in UI

---

## **Common Mistakes**

* Struct not matching JSON
* Ignoring nested structure
* Mixing API logic inside UI
* Not converting String to URL
* Not using AsyncImage
