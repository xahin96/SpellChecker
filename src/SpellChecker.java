import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SplayTree {
    class Node {
        String key;
        Node left, right, parent;

        Node(String key) {
            this.key = key;
            left = right = parent = null;
        }
    }

    Node root;

    // Rotate a node to the root position
    private void rotateToRoot(Node currentNode) {
        Node parentNode = currentNode.parent;
        if (currentNode == parentNode.left) {
            parentNode.left = currentNode.right;
            if (currentNode.right != null) {
                currentNode.right.parent = parentNode;
            }
            currentNode.right = parentNode;
        } else {
            parentNode.right = currentNode.left;
            if (currentNode.left != null) {
                currentNode.left.parent = parentNode;
            }
            currentNode.left = parentNode;
        }
        currentNode.parent = parentNode.parent;
        parentNode.parent = currentNode;
        if (currentNode.parent != null) {
            if (parentNode == currentNode.parent.left) {
                currentNode.parent.left = currentNode;
            } else {
                currentNode.parent.right = currentNode;
            }
        } else {
            root = currentNode;
        }
    }

    // Splay the node to the root
    private void splayToRoot(Node currentNode) {
        while (currentNode.parent != null) {
            Node parentNode = currentNode.parent;
            Node grandparentNode = parentNode.parent;
            if (grandparentNode == null) {
                rotateToRoot(currentNode);
            } else if ((parentNode.left == currentNode && grandparentNode.left == parentNode)
                    || (parentNode.right == currentNode && grandparentNode.right == parentNode)) {
                rotateToRoot(parentNode);
                rotateToRoot(currentNode);
            } else {
                rotateToRoot(currentNode);
                rotateToRoot(currentNode);
            }
        }
    }

    // Helper method for in-order traversal to collect all words
    private void collectWordsInOrder(Node node, List<String> wordList) {
        if (node != null) {
            collectWordsInOrder(node.left, wordList);
            wordList.add(node.key);
            collectWordsInOrder(node.right, wordList);
        }
    }

    // Get all words from the tree using in-order traversal
    public List<String> getAllWords() {
        List<String> wordList = new ArrayList<>();
        collectWordsInOrder(root, wordList);
        return wordList;
    }

    // Insert a new word into the tree
    public void insert(String word) {
        Node newNode = new Node(word);
        if (root == null) {
            root = newNode;
            return;
        }

        Node currentNode = root;
        Node parentNode = null;
        while (currentNode != null) {
            parentNode = currentNode;
            if (word.compareTo(currentNode.key) < 0) {
                currentNode = currentNode.left;
            } else if (word.compareTo(currentNode.key) > 0) {
                currentNode = currentNode.right;
            } else {
                // Word already exists; perform splay to make it the root
                splayToRoot(currentNode);
                return;
            }
        }

        if (word.compareTo(parentNode.key) < 0) {
            parentNode.left = newNode;
        } else {
            parentNode.right = newNode;
        }
        newNode.parent = parentNode;

        // Perform splay to make the new node the root
        splayToRoot(newNode);
    }

    // Search for a word in the tree
    public boolean search(String word) {
        Node node = searchNode(word);
        if (node != null) {
            splayToRoot(node); // Splay the found node to the root
            return true;
        }
        return false;
    }

    // Helper function to search for a word and return the node
    private Node searchNode(String word) {
        Node currentNode = root;
        while (currentNode != null) {
            int cmp = word.compareTo(currentNode.key);
            if (cmp == 0) {
                return currentNode;
            } else if (cmp < 0) {
                currentNode = currentNode.left;
            } else {
                currentNode = currentNode.right;
            }
        }
        return null;
    }

    // Delete a word from the tree
    public void delete(String word) {
        Node node = searchNode(word);
        if (node != null) {
            splayToRoot(node); // Splay the found node to the root

            // Remove the root
            if (node.left == null) {
                root = node.right;
                if (node.right != null) {
                    node.right.parent = null;
                }
            } else {
                Node rightSubtree = node.right;
                root = node.left;
                node.left.parent = null;
                Node currentNode = root;
                while (currentNode.right != null) {
                    currentNode = currentNode.right;
                }
                currentNode.right = rightSubtree;
                if (rightSubtree != null) {
                    rightSubtree.parent = currentNode;
                }
            }
        }
    }
}

public class SpellChecker {

    private static void populateDictionary(SplayTree dictionary) {
        // Adding correct words to the dictionary
        dictionary.insert("apple");
        dictionary.insert("banana");
        dictionary.insert("cherry");
        dictionary.insert("date");
        dictionary.insert("the");
        dictionary.insert("and");
        dictionary.insert("is");
        dictionary.insert("it");
        dictionary.insert("to");
        dictionary.insert("in");
        dictionary.insert("of");
        dictionary.insert("for");
        dictionary.insert("are");
        dictionary.insert("this");
        dictionary.insert("spell");
        dictionary.insert("checker");
        dictionary.insert("example");
        dictionary.insert("frequently");
        dictionary.insert("used");
        dictionary.insert("words");
        // Please add more words as needed
    }

    // Find a list of the closest matching words in the dictionary
    private static List<String> findClosestMatchingWords(String input, SplayTree dictionary, int maxDistanceThreshold, int maxMatches) {
        List<String> closestMatches = new ArrayList<>(maxMatches); // Initialize with empty strings
        for (int i = 0; i < maxMatches; i++) {
            closestMatches.add(""); // Add empty strings as placeholders
        }
        int[] minDistances = new int[maxMatches];
        for (int i = 0; i < maxMatches; i++) {
            minDistances[i] = Integer.MAX_VALUE;
        }

        for (String word : dictionary.getAllWords()) {
            int distance = calculateLevenshteinDistance(input, word);
            if (distance <= maxDistanceThreshold) {
                for (int i = 0; i < maxMatches; i++) {
                    if (distance < minDistances[i]) {
                        for (int j = maxMatches - 1; j > i; j--) {
                            minDistances[j] = minDistances[j - 1];
                            closestMatches.set(j, closestMatches.get(j - 1));
                        }
                        minDistances[i] = distance;
                        closestMatches.set(i, word);
                        break;
                    }
                }
            }
        }

        List<String> validMatches = new ArrayList<>();
        for (int i = 0; i < maxMatches; i++) {
            if (!closestMatches.get(i).isEmpty() && minDistances[i] != Integer.MAX_VALUE) {
                validMatches.add(closestMatches.get(i));
            }
        }

        return validMatches;
    }

    // Calculate the Levenshtein distance between two strings
    private static int calculateLevenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int substitutionCost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + substitutionCost);
                }
            }
        }

        return dp[m][n];
    }

    public static void main(String[] args) {
        SplayTree splayTree = new SplayTree();

        // Populate the dictionary with correct words
        populateDictionary(splayTree);

        Scanner scanner = new Scanner(System.in);

        // Welcome message
        System.out.println("   _____            ____   ________              __            ");
        System.out.println("  / ___/____  ___  / / /  / ____/ /_  ___  _____/ /_____  _____");
        System.out.println("  \\__ \\/ __ \\/ _ \\/ / /  / /   / __ \\/ _ \\/ ___/ //_/ _ \\/ ___/");
        System.out.println(" ___/ / /_/ /  __/ / /  / /___/ / / /  __/ /__/ ,< /  __/ /    ");
        System.out.println("/____/ .___/\\___/_/_/   \\____/_/ /_/\\___/\\___/_/|_|\\___/_/     ");
        System.out.println("    /_/                                                        ");
        System.out.print("\n~Welcome to the Spell Checker! Start writing to check your spelling game.~ \n\n");

        while (true) {
            System.out.print("Enter a word to check its spelling (or 'exit' to quit): ");

            // Taking user input
            String userInput = scanner.nextLine().trim().toLowerCase();

            if (userInput.equals("exit")) {
                System.out.print("Thank you for using Spell Checker! Goodbye :)");
                break;
            }

            // Levenshtein distance accuracy threshold parameter.
            // The higher the number, the more accurate match is being searched
            int maxDistanceThreshold = 2;

            // Looks for the number of matches found in the dictionary
            // Set to 2 for mimicking the functionality of MS Word
            int maxMatches = 2;

            if (splayTree.search(userInput)) {
                System.out.println("The word '" + userInput + "' is spelled correctly.");
            } else {
                List<String> suggestions = findClosestMatchingWords(userInput, splayTree, maxDistanceThreshold, maxMatches); // Suggest up to 2 matches
                if (!suggestions.isEmpty()) {
                    System.out.println("The word '" + userInput + "' is not found in the dictionary.");
                    System.out.println("Did you mean:");
                    for (String suggestion : suggestions) {
                        System.out.println("  - '" + suggestion + "'");
                    }
                } else {
                    System.out.println("The word '" + userInput + "' is not found in the dictionary.");
                    System.out.print("Do you want to add it to the dictionary? (yes/no): ");
                    String addWord = scanner.nextLine().trim().toLowerCase();
                    if (addWord.equals("yes")) {
                        splayTree.insert(userInput);
                        System.out.println("The word '" + userInput + "' has been added to the dictionary.");
                    } else {
                        System.out.println("The word '" + userInput + "' was not added to the dictionary.");
                    }
                }
            }
        }

        scanner.close();
    }
}
