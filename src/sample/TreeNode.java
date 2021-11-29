package sample;

class TreeNode implements Comparable<TreeNode> {

    Character content;
    int weight;
    TreeNode left;
    TreeNode right;

    public TreeNode(Character content, int weight) {
        this.content = content;
        this.weight = weight;
    }

    public TreeNode(Character content, int weight, TreeNode left, TreeNode right) {
        this.content = content;
        this.weight = weight;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(TreeNode o) {
        return o.weight - weight;
    }

    // извлечение кода для символа
    public String getCodeForCharacter(Character ch, String parentPath) {
        if (content == ch) {
            return  parentPath;
        } else {
            if (left != null) {
                String path = left.getCodeForCharacter(ch, parentPath + 0);
                if (path != null) {
                    return path;
                }
            }
            if (right != null) {
                return right.getCodeForCharacter(ch, parentPath + 1);
            }
        }
        return null;
    }
}
