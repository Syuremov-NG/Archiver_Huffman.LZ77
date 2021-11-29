package sample;

class Tuple {

    int offset;
    int stringLen;
    String nextChar;

    Tuple(int offset, int stringLen, String nextChar) {
        this.offset = offset;
        this.stringLen = stringLen;
        this.nextChar = nextChar;
    }

    public String toString() {
        return String.valueOf(offset) + stringLen + nextChar;
    }
}
