import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.swing.*;


public class Main {
    //frequency table
    static final class Node {
        String key;
        int freqCount;
        Node next;

        Node(String k, int c, Node n) {
            key = k;
            freqCount = c;
            next = n;
        }
    }

    //how a single file is represented
    static class wikiDoc {
        String url;
        String title;
        Node[] freqTable;
        Map<String, Double> tfidf;

        wikiDoc(String url, String title) {
            this.url = url;
            this.title = title;
            this.freqTable = new Node[128];
            this.tfidf = new HashMap<>();
        }
    }

    //results
    static class SimilarityResult {
        wikiDoc doc;
        double similarity;

        SimilarityResult(wikiDoc doc, double similarity) {
            this.doc = doc;
            this.similarity = similarity;
        }
    }

    // Collection of all documents
    static List<wikiDoc> allDocs = new ArrayList<>();

    //how many documents contain each term
    static Map<String, Integer> documentFrequency = new HashMap<>();


    public static void main(String[] args) throws IOException {

        File pages = new File("/Users/sakiyahwinston/Desktop/Fall2025/csc365/assingment_01/src/wikiPages/pages.txt");
        try (Scanner fileReader = new Scanner(pages)) {
            while (fileReader.hasNextLine()) {
                String url = fileReader.nextLine().trim();
                if (url.isEmpty()) continue;

                wikiDoc doc = extractPages(url);
                if (doc != null) {
                    allDocs.add(doc);
                    //System.out.println("  ✓ Loaded: " + doc.title);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error accord:" + e.getMessage());
        }

        getDocumentFreq();

        for (wikiDoc doc : allDocs) {
            calculateTFIDF(doc);
        }


        createGUI();
    }

    public static wikiDoc extractPages(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        String title = doc.title();
        Elements paragraphs = doc.select(".mw-parser-output > p");
        StringBuilder bodyText = new StringBuilder();
        for (Element p : paragraphs) {
            String text = p.text();
            if (!text.isEmpty()) {
                bodyText.append(text).append(" ");
            }
        }
        String text = bodyText.toString().trim();
        if (text.isEmpty()) return null;

        String[] words = text.toLowerCase().split("\\W+");

        wikiDoc webdoc = new wikiDoc(url, title);
        createTable(webdoc, words);

        //System.out.println(Arrays.toString(words));

        return webdoc;
    }


    public static void createTable(wikiDoc doc, String[] words) {
        Set<String> exludedWords = new HashSet<>(Arrays.asList("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "as", "is", "was", "are", "were", "be",
                "been", "has", "have", "had", "do", "does", "did", "will", "would",
                "could", "should", "may", "might", "can", "this", "that", "these",
                "those", "it", "its", "they", "them", "their"));

        int lenOfTable = doc.freqTable.length;
        for (String value : words) {
            if (exludedWords.contains(value) || value.isEmpty()) continue;
            int hash = value.hashCode();
            int index = hash & (lenOfTable - 1);

            boolean found = false;
            for (Node e = doc.freqTable[index]; e != null; e = e.next) {
                if (value.equals(e.key)) {
                    e.freqCount += 1;
                    found = true;
                    break;
                }
            }
            if (!found) {
                // insert new node at head with initial count 1
                doc.freqTable[index] = new Node(value, 1, doc.freqTable[index]);
            }
        }
    }

    public static void getDocumentFreq() {
        for (wikiDoc doc : allDocs) {
            Set<String> termsInDoc = new HashSet<>();

            //Collect all unique terms in this document
            for (Node buket : doc.freqTable) {
                for (Node e = buket; e != null; e = e.next) {
                    termsInDoc.add(e.key);
                }
            }

            for (String term : termsInDoc) {
                documentFrequency.put(term,
                        documentFrequency.getOrDefault(term, 0) + 1);
            }
        }
    }

    public static void calculateTFIDF(wikiDoc doc) {
        int totalWords = 0;
        for (Node buket : doc.freqTable) {
            for (Node e = buket; e != null; e = e.next) {
                totalWords += e.freqCount;
            }
        }
        int totalDocs = allDocs.size();

        //calc tfidf for each term
        for (Node buket : doc.freqTable) {
            for (Node e = buket; e != null; e = e.next) {
                String word = e.key;
                int wordFreq = e.freqCount;

                //Tf
                double tf = (double) wordFreq / totalWords;

                //idf
                int docsWithWord = documentFrequency.getOrDefault(word, 1);
                double idf = Math.log((double) totalDocs / docsWithWord);

                double tfidfv = tf * idf;

                doc.tfidf.put(word, tfidfv);


            }

        }
    }

    public static double cosineSimilarity(wikiDoc doc1, wikiDoc doc2) {
        Map<String, Double> vec1 = doc1.tfidf;
        Map<String, Double> vec2 = doc2.tfidf;

        double dotProduct = 0.0;
        for (String word : vec1.keySet()) {
            if (vec2.containsKey(word)) {
                dotProduct += vec1.get(word) * vec2.get(word);
            }
        }

        double magnitude1 = 0.0;
        for (double value : vec1.values()) {
            magnitude1 += value * value;
        }
        magnitude1 = Math.sqrt(magnitude1);

        double magnitude2 = 0.0;
        for (double value : vec2.values()) {
            magnitude2 += value * value;
        }
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }


    public static List<SimilarityResult> getSimilarDocs(wikiDoc query){
        List<SimilarityResult> results = new ArrayList<>();

        for (wikiDoc doc : allDocs) {
            if (doc == query) continue;
            double similarity = cosineSimilarity(query, doc);
            results.add(new SimilarityResult(doc, similarity));
        }

        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        return results.subList(0, Math.min(2, results.size()));
    }



    public static void createGUI(){
        //creating main window
        JFrame frame = new JFrame("Wikipedia Similarity");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        //creating main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Wikipedia Similarity");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        //page dropdown and button
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        JLabel instructionLabel = new JLabel("Select a topic to find 2 most similar pages:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, BorderLayout.NORTH);

        String[] docTitles = new String[allDocs.size()];
        for (int i = 0; i <allDocs.size(); i++){
            docTitles[i] = allDocs.get(i).title;
        }
        JComboBox<String> docDropdown = new JComboBox<>(docTitles);
        docDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(docDropdown, BorderLayout.CENTER);

        JButton findButton = new JButton("Find similar pages");
        findButton.setFont(new Font("Arial", Font.BOLD, 14));
        findButton.setBackground(new Color(70, 130, 180));
        findButton.setForeground(Color.BLACK);
        centerPanel.add(findButton, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        //results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        scrollPane.setPreferredSize(new Dimension(650, 250));

        //button listener
        findButton.addActionListener(e -> {
            resultsPanel.removeAll();

            int selectedIndex = docDropdown.getSelectedIndex();

            if(selectedIndex >= 0){
                wikiDoc selectedDoc = allDocs.get(selectedIndex);
                JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel queryLabel = new JLabel("Seleceted: " + selectedDoc.title);
                queryLabel.setFont(new Font("Arial", Font.BOLD, 14));
                queryPanel.add(queryLabel);
                resultsPanel.add(queryPanel);
                resultsPanel.add(Box.createVerticalStrut(10));

                List<SimilarityResult> results = getSimilarDocs(selectedDoc);

                for(int i =0; i < results.size(); i++){
                    SimilarityResult result = results.get(i);
                    JPanel resultPanel = new JPanel();
                    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
                    //
                    resultPanel.setMaximumSize(new Dimension(650, 100));
                    JLabel rankLabel = new JLabel("Rank #" + (i + 1));
                    rankLabel.setFont(new Font("Arial", Font.BOLD, 12));
                    rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    resultPanel.add(rankLabel);

                    JLabel titleLabelResult = new JLabel(result.doc.title);
                    titleLabelResult.setFont(new Font("Arial", Font.BOLD, 14));
                    titleLabelResult.setAlignmentX(Component.LEFT_ALIGNMENT);
                    resultPanel.add(titleLabelResult);

                    JLabel simLabel = new JLabel(String.format("Similarity Score: %.4f (out of 1.0)", result.similarity));
                    simLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    simLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    resultPanel.add(simLabel);

                    resultsPanel.add(resultPanel);
                    resultsPanel.add(Box.createVerticalStrut(10));

                }

            }
            resultsPanel.revalidate();
            resultsPanel.repaint();

        });

        frame.add(mainPanel);
        frame.setVisible(true);

    }

}









