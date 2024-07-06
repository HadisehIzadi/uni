import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class code {

    public static void main(String[] args) {
        String productFile = "product.csv";
        String queryFile = "query.csv";


        List<Product> products = preprocessProducts(productFile);

        List<Query> queries = preprocessQueries(queryFile);

        Map<String, Double> idfMap = calculateIDF(products, queries);

        Scanner scanner = new Scanner(System.in);
        System.out.print("enter your query plz ");
        String userInput = scanner.nextLine();

        String[] processedQueryText = preprocessText(userInput);
        Query userQuery = new Query("user_query", userInput, processedQueryText);

        Map<Product, Double> tfidfScores = calculateTFIDF(userQuery, products, idfMap);
        List<Product> sortedProducts = sortProductsByTFIDF(tfidfScores);

        System.out.println("\nproducts related to query  '" + userInput + "':");
        for (int i = 0; i < Math.min(25, sortedProducts.size()); i++) {
            System.out.println(sortedProducts.get(i).getName() + " - TFIDF Score: " + tfidfScores.get(sortedProducts.get(i)));
        }
    }

    public static List<Product> preprocessProducts(String fileName) {
        List<Product> products = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                String[] tokens = new String[tokenizer.countTokens()];
                int index = 0;
                while (tokenizer.hasMoreTokens()) {
                    tokens[index++] = tokenizer.nextToken();
                }

                if (tokens.length >= 2) {
                    String[] processedDescription = preprocessText(tokens[1]);
                    Product product = new Product(tokens[0], tokens[1], processedDescription);
                    products.add(product);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return products;
    }

    public static List<Query> preprocessQueries(String fileName) {
        List<Query> queries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                String[] tokens = new String[tokenizer.countTokens()];

                int index = 0;
                while (tokenizer.hasMoreTokens()) {
                    tokens[index++] = tokenizer.nextToken();
                }

                if (tokens.length >= 2) {
                    String[] processedQueryText = preprocessText(tokens[1]);
                    Query query = new Query(tokens[0], tokens[1], processedQueryText);
                    queries.add(query);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queries;
    }

    public static String[] preprocessText(String text) {

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens.toArray(new String[0]);
    }

    public static Map<String, Double> calculateIDF(List<Product> products, List<Query> queries) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        Map<String, Double> idfMap = new HashMap<>();
        int totalDocuments = products.size() + queries.size();

        for (Product product : products) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(product.getProcessedDescription()));
            for (String word : uniqueWords) {
                if (documentFrequency.containsKey(word)) {
                    documentFrequency.put(word, documentFrequency.get(word) + 1);
                } else {
                    documentFrequency.put(word, 1);
                }
            }
        }

        for (Query query : queries) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(query.getProcessedQueryText()));
            for (String word : uniqueWords) {
                if (documentFrequency.containsKey(word)) {
                    documentFrequency.put(word, documentFrequency.get(word) + 1);
                } else {
                    documentFrequency.put(word, 1);
                }
            }
        }

        for (String word : documentFrequency.keySet()) {
            double idf = Math.log((double) totalDocuments / (double) documentFrequency.get(word));
            idfMap.put(word, idf);
        }

        return idfMap;
    }

    public static Map<Product, Double> calculateTFIDF(Query query, List<Product> products, Map<String, Double> idfMap) {
        Map<Product, Double> tfidfScores = new HashMap<>();
        Set<String> queryWords = new HashSet<>(Arrays.asList(query.getProcessedQueryText()));

        for (Product product : products) {
            double tfidfScore = 0.0;
            for (String word : product.getProcessedDescription()) {
                if (queryWords.contains(word)) {
                    double tf = (double) Collections.frequency(Arrays.asList(product.getProcessedDescription()), word);
                    double idf = idfMap.getOrDefault(word, 0.0);
                    tfidfScore += tf * idf;
                }
            }
            tfidfScores.put(product, tfidfScore);
        }

        return tfidfScores;
    }

    public static List<Product> sortProductsByTFIDF(Map<Product, Double> tfidfScores) {
        List<Map.Entry<Product, Double>> sortedList = new ArrayList<>(tfidfScores.entrySet());

        Collections.sort(sortedList, new Comparator<Map.Entry<Product, Double>>() {
            @Override
            public int compare(Map.Entry<Product, Double> o1, Map.Entry<Product, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<Product> sortedProducts = new ArrayList<>();
        for (Map.Entry<Product, Double> entry : sortedList) {
            sortedProducts.add(entry.getKey());
        }
        return sortedProducts;
    }

    static class Product {
        private String productId;
        private String name;
        private String[] processedDescription;

        public Product(String productId, String name, String[] processedDescription) {
            this.productId = productId;
            this.name = name;
            this.processedDescription = processedDescription;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public String[] getProcessedDescription() {
            return processedDescription;
        }
    }

    static class Query {
        private String queryId;
        private String queryText;
        private String[] processedQueryText;

        public Query(String queryId, String queryText, String[] processedQueryText) {
            this.queryId = queryId;
            this.queryText = queryText;
            this.processedQueryText = processedQueryText;
        }

        public String getQueryId() {
            return queryId;
        }

        public String getQueryText() {
            return queryText;
        }

        public String[] getProcessedQueryText() {
            return processedQueryText;
        }
    }
}
