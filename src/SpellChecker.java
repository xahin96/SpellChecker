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

    // Zig rotation
    private void zig(Node x) {
        Node y = x.parent;
        if (x == y.left) {
            y.left = x.right;
            if (x.right != null) {
                x.right.parent = y;
            }
            x.right = y;
        } else {
            y.right = x.left;
            if (x.left != null) {
                x.left.parent = y;
            }
            x.left = y;
        }
        x.parent = y.parent;
        y.parent = x;
        if (x.parent != null) {
            if (y == x.parent.left) {
                x.parent.left = x;
            } else {
                x.parent.right = x;
            }
        } else {
            root = x;
        }
    }

    // Zig-zig rotation
    private void zigZig(Node x) {
        zig(x.parent);
        zig(x);
    }

    // Zig-zag rotation
    private void zigZag(Node x) {
        zig(x);
        zig(x);
    }

    // Splay operation
    private void splay(Node x) {
        while (x.parent != null) {
            Node parent = x.parent;
            Node grandparent = parent.parent;
            if (grandparent == null) {
                zig(x);
            } else if ((parent.left == x && grandparent.left == parent) || (parent.right == x && grandparent.right == parent)) {
                zigZig(x);
            } else {
                zigZag(x);
            }
        }
    }


    // Helper method for in-order traversal to collect all words
    private void inOrderTraversalCollectWords(Node node, List<String> wordList) {
        if (node != null) {
            inOrderTraversalCollectWords(node.left, wordList);
            wordList.add(node.key);
            inOrderTraversalCollectWords(node.right, wordList);
        }
    }

    // Get all words from the tree using in-order traversal
    public List<String> getAllWords() {
        List<String> wordList = new ArrayList<>();
        inOrderTraversalCollectWords(root, wordList);
        return wordList;
    }

    // Insert a new key
    public void insert(String key) {
        Node newNode = new Node(key);
        if (root == null) {
            root = newNode;
            return;
        }

        Node current = root;
        Node parent = null;
        while (current != null) {
            parent = current;
            if (key.compareTo(current.key) < 0) {
                current = current.left;
            } else if (key.compareTo(current.key) > 0) {
                current = current.right;
            } else {
                // Key already exists; perform splay to make it the root
                splay(current);
                return;
            }
        }

        if (key.compareTo(parent.key) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        newNode.parent = parent;

        // Perform splay to make the new node the root
        splay(newNode);
    }

    // Search for a key
    public boolean search(String key) {
        Node node = searchNode(key);
        if (node != null) {
            splay(node); // Splay the found node to the root
            return true;
        }
        return false;
    }

    // Helper function to search for a key and return the node
    private Node searchNode(String key) {
        Node current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    // Delete a key
    public void delete(String key) {
        Node node = searchNode(key);
        if (node != null) {
            splay(node); // Splay the found node to the root

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
                Node current = root;
                while (current.right != null) {
                    current = current.right;
                }
                current.right = rightSubtree;
                if (rightSubtree != null) {
                    rightSubtree.parent = current;
                }
            }
        }
    }
}

public class SpellChecker {
    private static void populateDictionary(SplayTree dictionary) {
        // Add correct words to the dictionary
        dictionary.insert("apple");
        dictionary.insert("banana");
        dictionary.insert("cherry");
        dictionary.insert("date");
        // Add more words as needed
    }

    private static String findClosestMatch(String input, SplayTree splayTree) {
        // Initialize variables to keep track of the best match
        String closestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        // Perform a search in the dictionary for suggestions
        for (String word : splayTree.getAllWords()) {
            int distance = computeLevenshteinDistance(input, word);
            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = word;
            }
        }

        // Return the closest match if found
        return closestMatch;
    }

    private static int computeLevenshteinDistance(String s1, String s2) {
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

        while (true) {
            System.out.print("Enter a word to check its spelling (or 'exit' to quit): ");
            String userInput = scanner.nextLine().trim().toLowerCase();

            if (userInput.equals("exit")) {
                break;
            }
            if (splayTree.search(userInput)) {
                System.out.println("The word '" + userInput + "' is spelled correctly.");
            } else {
                String suggestion = findClosestMatch(userInput, splayTree);
                if (suggestion != null) {
                    System.out.println("The word '" + userInput + "' is not found in the dictionary.");
                    System.out.println("Did you mean '" + suggestion + "'?");
                } else {
                    System.out.println("The word '" + userInput + "' is not found in the dictionary, and no suggestions were found.");
                }
            }
        }
        scanner.close();
    }
}
