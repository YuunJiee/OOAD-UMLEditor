package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.CompositeObject;
import model.Groupable;
import model.UMLObject;
import model.PortOwner;

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

    public void deleteSelected() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.isEmpty())
            return;

        Set<PortOwner> deleteOwners = new java.util.HashSet<>();
        for (UMLObject obj : selected) {
            obj.collectDeletingPortOwners(deleteOwners);
        }

        objects.removeIf(o -> o.shouldBeDeleted(deleteOwners));
    }

    public void bringToFront(UMLObject obj) {
        if (objects.remove(obj)) {
            obj.setZIndex(99);
            objects.add(obj);   // Keep at end of list for ties
        }
    }

    public void deselectAll() {
        objects.forEach(o -> o.setSelected(false));
    }

    public List<UMLObject> getSelectedObjects() {
        return objects.stream()
                .filter(UMLObject::isSelected)
                .collect(Collectors.toList());
    }

    private <T> T getTopmost(java.util.function.Function<UMLObject, T> mapper) {
        T best = null;
        int bestIndex = -1;
        int bestZ = Integer.MIN_VALUE;
        for (int i = 0; i < objects.size(); i++) {
            UMLObject obj = objects.get(i);
            T mapped = mapper.apply(obj);
            if (mapped == null) continue;
            
            int z = obj.getZIndex();
            if (z > bestZ || (z == bestZ && i > bestIndex)) {
                best = mapped;
                bestZ = z;
                bestIndex = i;
            }
        }
        return best;
    }

    public UMLObject getTopmostSelectableAt(int x, int y) {
        return getTopmost(obj -> {
            if (!(obj instanceof Groupable) || !obj.contains(x, y)) return null;
            return obj;
        });
    }

    public PortOwner getPortOwnerByPort(int mx, int my) {
        return getTopmost(obj -> {
            if (obj instanceof PortOwner po && po.getPortAt(mx, my) != null) return po;
            return null;
        });
    }

    public boolean groupSelected() {
        List<UMLObject> groundable = objects.stream()
                .filter(o -> o.isSelected() && o instanceof Groupable)
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
