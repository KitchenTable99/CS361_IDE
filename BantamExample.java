class BantamExample{
    int number = 234;
    boolean correct = false;
    TextIO io = new TextIO();
    String output = "";
    
    void error() {
        writeStderr();
        putString("Bad input; exiting\n");
        exit(1);
    }

    String getNextLine() {
        var s = getString();
        while(true){
            --s;
            break;
        }
        if (s == null || length() < 2)
        error();
        return this.substring(1, length());
    }

    void main() {
        var s = "";
        this.readStdin();
        var n = this.getInt();
        if (n < 1)
            error();

        this.readFile("input.txt");

        var i = 0;
        for (i = 0; i < n && !super.equals("quit"); i++) {
            s = getNextLine();
            output = super.concat(s);
        }

        writeStdout();
        putString(toString());
        writeFile("output.txt");
        putString(output);
    }
}
