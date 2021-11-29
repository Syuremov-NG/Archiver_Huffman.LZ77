package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Controller {

    ArrayList<TreeNode> treeNodes = new ArrayList<>();
    @FXML
    private TextField compPathText;
    @FXML
    private TextField decompPathText;
    @FXML
    private TextArea textArea;

    private static TreeMap<Character, Integer> countFreq(String text) {
        TreeMap<Character, Integer> freqMap = new TreeMap<>();
        for (int i = 0; i < text.length(); i++) {
            Character c = text.charAt(i);
            Integer count = freqMap.get(c);
            freqMap.put(c, count != null ? count + 1 : 1);
        }
        return freqMap;
    }

    private static TreeNode huffman(ArrayList<TreeNode> treeNodes) {
        while (treeNodes.size() > 1) {
            Collections.sort(treeNodes);
            TreeNode left = treeNodes.remove(treeNodes.size() - 1);
            TreeNode right = treeNodes.remove(treeNodes.size() - 1);

            TreeNode parent = new TreeNode(null, right.weight + left.weight, left, right);
            treeNodes.add(parent);
        }
        return treeNodes.get(0);
    }

    private static String huffmanDecode(String encoded, TreeNode tree) {
        StringBuilder decoded = new StringBuilder();

        TreeNode node = tree;
        for (int i = 0; i < encoded.length(); i++) {
            node = encoded.charAt(i) == '0' ? node.left : node.right;
            if (node.content != null) {
                decoded.append(node.content);
                node = tree;
            }
        }
        return decoded.toString();
    }

    private static void save(File output, Map<Character, Integer> frequencies, String bits) {
        try {
            DataOutputStream os = new DataOutputStream(new FileOutputStream(output));
            os.writeInt(frequencies.size());
            for (Character character : frequencies.keySet()) {
                os.writeChar(character);
                os.writeInt(frequencies.get(character));
            }
            int compressedSizeBits = bits.length();
            BitArray bitArray = new BitArray(compressedSizeBits);
            for (int i = 0; i < bits.length(); i++) {
                bitArray.set(i, bits.charAt(i) != '0' ? 1 : 0);
            }
            os.writeInt(compressedSizeBits);
            os.write(bitArray.bytes, 0, bitArray.getSizeInBytes());
            os.flush();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load(File input, Map<Character, Integer> frequencies, StringBuilder bits) {
        try {
            DataInputStream os = new DataInputStream(new FileInputStream(input));
            int frequencyTableSize = os.readInt();
            for (int i = 0; i < frequencyTableSize; i++) {
                frequencies.put(os.readChar(), os.readInt());
            }
            int dataSizeBits = os.readInt();
            BitArray bitArray = new BitArray(dataSizeBits);
            os.read(bitArray.bytes, 0, bitArray.getSizeInBytes());
            os.close();

            for (int i = 0; i < bitArray.size; i++) {
                bits.append(bitArray.get(i) != 0 ? "1" : 0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        if (event.getSource().toString().equals("Button[id=compPathBtn, styleClass=button]'...'")) {
            System.out.println(event.getSource().toString());
//            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt", "*.txt"));
            File buff = fileChooser.showOpenDialog(textArea.getScene().getWindow());
            compPathText.setText(buff.getAbsolutePath());
        }
        if (event.getSource().toString().equals("Button[id=decompPathBtn, styleClass=button]'...'")) {
            System.out.println(event.getSource().toString());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("cpr", "*.cpr"));
            File buff = fileChooser.showOpenDialog(textArea.getScene().getWindow());
            decompPathText.setText(buff.getAbsolutePath());
        }
    }

    @FXML
    void compress() throws IOException {
        treeNodes.clear();
        String content = Lz77.compress(compPathText.getText());
        File inputFile = new File(compPathText.getText());

        TreeMap<Character, Integer> freq = countFreq(content); // Считаем частоты

        for (Character c : freq.keySet()) {
            treeNodes.add(new TreeNode(c, freq.get(c))); // Составляем листья для символов
        }

        TreeNode tree = huffman(treeNodes); // Построение дерева


        TreeMap<Character, String> codes = new TreeMap<>(); // Таблица кодов
        for (Character c : freq.keySet()) {
            codes.put(c, tree.getCodeForCharacter(c, ""));
        }

        StringBuilder encoded = new StringBuilder(); // Закодированная таблица
        for (int i = 0; i < content.length(); i++) {
            encoded.append(codes.get(content.charAt(i)));
        }

        // сохранение сжатой информации в файл
        File file = new File(compPathText.getText() + ".cpr");
        save(file, freq, encoded.toString());

        textArea.setText("Путь к сжатому файлу: " + file.getAbsolutePath() + "\n" +
                "Коэффицент сжатия: " + ((float) file.length() / inputFile.length()));
        decompPathText.setText(file.getAbsolutePath());
    }

    @FXML
    void extract() throws IOException {
        TreeMap<Character, Integer> frequencies2 = new TreeMap<>();
        StringBuilder encoded2 = new StringBuilder();
        treeNodes.clear();

        load(new File(decompPathText.getText()), frequencies2, encoded2);

        // генерация листов и постоение кодового дерева Хаффмана на основе таблицы частот сжатого файла
        for (Character c : frequencies2.keySet()) {
            treeNodes.add(new TreeNode(c, frequencies2.get(c)));
        }
        TreeNode tree2 = huffman(treeNodes);

        // декодирование обратно исходной информации из сжатой
        String decoded = huffmanDecode(encoded2.toString(), tree2);

        String extractedString = Lz77.decompress(decoded);

        // сохранение в файл декодированной информации
        File outFile = new File(decompPathText.getText() + ".txt");
        FileWriter fileWriter = new FileWriter(outFile, true);
        fileWriter.write(extractedString);
        fileWriter.close();

        textArea.setText("Путь к распакованному файлу " + outFile.getAbsolutePath());
    }
}