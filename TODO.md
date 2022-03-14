# TODO

## Access Check
* Matt
~~* Ian~~

## Fixes from Project 5
* add file header to SaveReason.java
* add file header to DialogHelper.java
* rename HighlightedCodeArea
  * JavaCodeArea seems like a good candidate but Dale seemed to indicate even this isn't enough, so I don't know what it should be called
  * Having CodeArea in the name indicates this is a subclass of CodeArea which it isn't
* Controller class does too much—break it into smaller pieces
* Handle the IOException thrown in Main.java
## Bugs that Dale found
* Running a program that contains an infinite loop of print statements prints nothing and freezes
* When compiling and running the example Scanner thing from Moodle, the print statements don't seem to work
  * The file on Dale's side also immediately crashed upon execution completion. I can't replicate that bug.
  * It is caused by your use of inputReader.readLine() on approximately line 662 of Controller.  That method reads input until it encounters \n or \r.  The JavaScannerExample prints out the two prompts using System.out.print, not System.out.println, and so no \n nor \r are output by that program and hence no \n or \r appears in the inputReader’s stream.