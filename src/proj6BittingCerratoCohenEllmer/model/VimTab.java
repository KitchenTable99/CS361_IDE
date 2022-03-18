package proj6BittingCerratoCohenEllmer.model;

import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

public class VimTab extends Tab {

    private boolean inVIMCommandMode;
    private String vimCommands = "";

    public VimTab(String tabName) {
        super(tabName);

        EventHandler<KeyEvent> vimHandler = new EventHandler<>() {
            /**
             * Route VIM key press events to implemented methods
             *
             * @param key A KeyEvent object that gives information about a Key Stroke
             */
            @Override
            public void handle(KeyEvent key) {
                // Escape -> command mode
                if (!inVIMCommandMode && KeyCode.ESCAPE.equals(key.getCode())) {
                    inVIMCommandMode = true;
                }
                if (inVIMCommandMode) {
                    // must handle all key events to suppress keystroke in codeArea
                    // Only read key released to prevent duplicated commands
                    if (key.getEventType().equals(KeyEvent.KEY_RELEASED)) {
                        vimCommands += key.getText();
                        System.out.println("Vim Commands: " + vimCommands);
                        dispatchVimCommand(vimCommands);
                        key.consume();
                    }
                }
            }
        };

        JavaCodeArea javaCodeArea = new JavaCodeArea();
        CodeArea codeArea = javaCodeArea.getCodeArea();
        codeArea.addEventFilter(KeyEvent.ANY, vimHandler);
        setContent(new VirtualizedScrollPane<>(codeArea));
    }

    private void dispatchVimCommand(String commands) {
    }
}
