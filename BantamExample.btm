class BantamExample{
    int number = 234;
    boolean correct = false;
    TextIO io = new TextIO();
    String output = "";
    
    void error() {
        io.writeStderr();
        io.putString("Bad input; exiting\n");
        Sys.exit(1);
    }

    String getNextLine() {
        var s = io.getString();
        while(true){
            --s;
            break;
        }
        if (s == null || s.length() < 2)
        error();
        return s.substring(1, s.length());
    }

    void main() {
        var s = "";
        io.readStdin();
        var n = io.getInt();
        if (n < 1)
            error();

        io.readFile("input.txt");

        var i = 0;
        for (i = 0; i < n && !s.equals("quit"); i++) {
            s = getNextLine();
            output = output.concat(s);
        }

        io.writeStdout();
        io.putString(output.toString());
        io.writeFile("output.txt");
        io.putString(output);
    }
}
