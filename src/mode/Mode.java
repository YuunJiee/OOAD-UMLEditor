package mode;

public enum Mode {
    SELECT("Select", "Select  —  click to select · drag on object to move · drag on port to resize"),
    ASSOCIATION("Association", "Association  —  drag from a port to another port"),
    GENERALIZATION("Generalization", "Generalization  —  drag from a port to another port"),
    COMPOSITION("Composition", "Composition  —  drag from a port to another port"),
    RECT("Rect", "Rect  —  drag on canvas to draw"),
    OVAL("Oval", "Oval  —  drag on canvas to draw");

    public final String label;
    public final String statusText;

    Mode(String label, String statusText) {
        this.label = label;
        this.statusText = statusText;
    }

    public boolean isLinkMode() {
        return this == ASSOCIATION || this == GENERALIZATION || this == COMPOSITION;
    }
}