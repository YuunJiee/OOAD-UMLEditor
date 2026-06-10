package mode;

import java.util.function.BiPredicate;

import model.PortOwner;
import model.UMLObject;

public enum Mode {
    SELECT("Select", "Select  —  click to select · drag on object to move · drag on port to resize",
            (obj, hovered) -> obj == hovered),
    ASSOCIATION("Association", "Association  —  drag from a port to another port",
            (obj, hovered) -> obj instanceof PortOwner),
    GENERALIZATION("Generalization", "Generalization  —  drag from a port to another port",
            (obj, hovered) -> obj instanceof PortOwner),
    COMPOSITION("Composition", "Composition  —  drag from a port to another port",
            (obj, hovered) -> obj instanceof PortOwner),
    RECT("Rect", "Rect  —  drag on canvas to draw",
            (obj, hovered) -> false),
    OVAL("Oval", "Oval  —  drag on canvas to draw",
            (obj, hovered) -> false);

    public final String label;
    public final String statusText;
    private final BiPredicate<UMLObject, UMLObject> portPredicate;

    Mode(String label, String statusText, BiPredicate<UMLObject, UMLObject> portPredicate) {
        this.label = label;
        this.statusText = statusText;
        this.portPredicate = portPredicate;
    }

    public boolean shouldShowPortsFor(UMLObject obj, UMLObject hovered) {
        return portPredicate.test(obj, hovered);
    }
}
