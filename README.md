# Wikipedia-Similarity-Analyzer-
A Java-based application that analyzes Wikipedia articles and identifies the most similar pages using TF-IDF (Term Frequency-Inverse Document Frequency) vectorization and cosine similarity metrics. This was built for a class, CSC365 Data Structures and Algorithms. 

## Features
- **Web Scraping**: Automatically fetches and parses Wikipedia articles using JSoup
- **TF-IDF Vectorization**: Calculates term importance across a document corpus
- **Cosine Similarity**: Measures document similarity using vector space modeling
- **Custom Hash Table**: Efficient term frequency storage with collision handling
- **Interactive GUI**: Swing-based interface for selecting articles and viewing results
- **Real-time Analysis**: Compares the selected article against the entire corpus instantly

## Technologies Used
- **Java** - Core programming language
- **JSoup** - HTML parsing and web scraping
- **Swing** - GUI framework
- **Custom Data Structures** - Hash table implementation for frequency counting

##  How It Works
1. **Data Collection**: Reads Wikipedia URLs from a text file and scrapes article content
2. **Preprocessing**: Tokenizes text, removes stop words, and builds frequency tables
3. **TF-IDF Calculation**: 
   - Computes Term Frequency (TF) for each word in each document
   - Calculates Inverse Document Frequency (IDF) across the corpus
   - Multiplies TF × IDF to get term importance scores
4. **Similarity Scoring**: Uses cosine similarity to compare TF-IDF vectors between documents
5. **Ranking**: Displays top 2 most similar articles with similarity scores (0.0 - 1.0)

## Getting Started

### Prerequisites
- Java 8 or higher
- JSoup library

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/wikipedia-similarity-analyzer.git
cd wikipedia-similarity-analyzer
```

2. Add JSoup to your project dependencies

3. Create a `pages.txt` file with Wikipedia URLs (one per line)
```
https://en.wikipedia.org/wiki/Machine_learning
https://en.wikipedia.org/wiki/Artificial_intelligence
https://en.wikipedia.org/wiki/Deep_learning
```

4. Update the file path in `Main.java`:
```java
File pages = new File("path/to/your/pages.txt");
```

5. Compile and run
```bash
javac Main.java
java Main
```

## 💡Usage
1. Launch the application
2. Select a Wikipedia article from the dropdown menu
3. Click "Find similar pages"
4. View the top 2 most similar articles with their similarity scores

## 📈 Algorithm Details
### TF-IDF Formula
- **TF (Term Frequency)**: `wordCount / totalWords`
- **IDF (Inverse Document Frequency)**: `log(totalDocs / docsContainingTerm)`
- **TF-IDF**: `TF × IDF`

### Cosine Similarity
```
similarity = (A · B) / (||A|| × ||B||)
```
Where A and B are TF-IDF vectors of two documents.

## Screenshots
<img width="701" height="504" alt="Screenshot 2025-10-07 at 1 25 34 PM" src="https://github.com/user-attachments/assets/b6cb0584-a65c-4a4f-ad91-3f659797a956" />

## Future Enhancements
- adding persistent data stores


- GitHub: https://github.com/sakiw3484
- LinkedIn: www.linkedin.com/in/sakiyah-winston





⭐ Star this repo if you find it helpful!
