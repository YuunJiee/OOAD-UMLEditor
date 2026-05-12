package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.BasicObject;
import model.CompositeObject;
import model.LinkObject;
import model.UMLObject;

public class CanvasModel {
    private final List<UMLObject> objects = new ArrayList<>();

    public void add(UMLObject obj) {
        objects.add(obj);
    }

    public void addAtBottom(UMLObject obj) {
        objects.add(0, obj);
    }

    public void remove(UMLObject obj) {
        objects.remove(obj);
    }

    public void addAllAt(int index, List<UMLObject> objs) {
        objects.addAll(Math.min(index, objects.size()), objs);
    }

    public int indexOf(UMLObject obj) {
        return objects.indexOf(obj);
    }

    public List<UMLObject> getAll() {
        return Collections.unmodifiableList(objects);
    }

    public List<UMLObject> getLinks() {
        return objects.stream()
                .filter(o -> o instanceof LinkObject)
                .collect(Collectors.toList());
    }

    public List<UMLObject> getNonLinks() {
        return objects.stream()
                .filter(o -> !(o instanceof LinkObject))
                .collect(Collectors.toList());
    }

    public void deleteSelected() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.isEmpty())
            return;

        Set<BasicObject> deleteBasics = new java.util.HashSet<>();
        for (UMLObject obj : selected) {
            if (obj instanceof BasicObject bo) {
                deleteBasics.add(bo);
            } else if (obj instanceof CompositeObject co) {
                deleteBasics.addAll(co.collectAllBasicObjects());
            }
        }

        objects.removeIf(o -> {
            if (o instanceof LinkObject lo) {
                return lo.isSelected() ||
                        deleteBasics.contains(lo.getFromObject()) ||
                        deleteBasics.contains(lo.getToObject());
            }
            return o.isSelected();
        });
    }

    public void bringToFront(UMLObject obj) {
        objects.remove(obj);
        objects.add(obj);
    }

    public void deselectAll() {
        objects.forEach(o -> o.setSelected(false));
    }

    public List<UMLObject> getSelectedObjects() {
        return objects.stream()
                .filter(UMLObject::isSelected)
                .collect(Collectors.toList());
    }

    public UMLObject getTopmostNonLinkAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof LinkObject)
                continue;
            if (obj.contains(x, y))
                return obj;
        }
        return null;
    }

    public BasicObject getBasicObjectByPort(int mx, int my) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (objects.get(i) instanceof BasicObject bo
                    && bo.getPortIndexAt(mx, my) >= 0) {
                return bo;
            }
        }
        return null;
    }

    public boolean groupSelected() {
        List<UMLObject> groundable = objects.stream()
                .filter(o -> o.isSelected() && !(o instanceof LinkObject))
                .collect(Collectors.toList());
        if (groundable.size() < 2)
            return false;

        groundable.forEach(o -> o.setSelected(false));
        CompositeObject composite = new CompositeObject(groundable);

        objects.removeAll(groundable);

        deselectAll();
        objects.add(composite);
        composite.setSelected(true);

        return true;
    }

    public boolean ungroupSelected() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject co))
            return false;

        int idx = objects.indexOf(co);
        objects.remove(co);
        List<UMLObject> children = new ArrayList<>(co.getChildren());
        addAllAt(idx, children);
        deselectAll();
        return true;
    }
}
