# UML Editor — MVC 重構：Model 設計

## 設計原則

在 MVC 架構中，Model 負責：
- 持有所有 UML 物件的狀態
- 提供操作資料的 API（新增、刪除、移動、群組）
- 透過 Observer pattern 通知 View 資料已改變

Model **不知道** 任何畫面細節（Graphics、JPanel、滑鼠事件）。

---

## Observer Interface

```java
public interface ModelListener {
    void onModelChanged();
}
```

View 實作此介面，向 Model 註冊後，每次 Model 改變都會收到通知並觸發 repaint。

---

## UMLModel Class Template

```java
public class UMLModel {

    private final List<UMLObject> objects = new ArrayList<>();
    private final List<ModelListener> listeners = new ArrayList<>();

    // ── Observer ──────────────────────────────────────────

    public void addListener(ModelListener l) {
        listeners.add(l);
    }

    private void notifyChanged() {
        listeners.forEach(ModelListener::onModelChanged);
    }

    // ── Query API ─────────────────────────────────────────

    public List<UMLObject> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    public UMLObject getTopmostAt(int x, int y) {
        // iterate objects by descending zIndex
        // return first non-link whose contains(x, y) is true
    }

    // ── Mutation API ──────────────────────────────────────

    public void addObject(UMLObject obj) {
        objects.add(obj);
        notifyChanged();
    }

    public void moveSelected(int dx, int dy) {
        objects.stream()
               .filter(UMLObject::isSelected)
               .forEach(o -> o.move(dx, dy));
        notifyChanged();
    }

    public void deleteSelected() {
        Set<PortOwner> deletingOwners = new HashSet<>();
        objects.stream()
               .filter(UMLObject::isSelected)
               .forEach(o -> o.collectDeletingPortOwners(deletingOwners));
        objects.removeIf(o -> o.shouldBeDeleted(deletingOwners));
        notifyChanged();
    }

    public void groupSelected() {
        List<UMLObject> groupable = objects.stream()
                .filter(o -> o.isSelected() && !(o instanceof LinkObject))
                .collect(Collectors.toList());
        if (groupable.size() < 2) return;
        objects.removeAll(groupable);
        objects.add(new CompositeObject(groupable));
        notifyChanged();
    }
}
```

---

## MVC 對應關係

| 角色 | 對應類別 | 職責 |
|------|---------|------|
| Model | `UMLModel` | 持有 UMLObject 列表；提供 mutating API；通知 listeners |
| View | `Canvas` (JPanel) | 實作 `ModelListener`；收到通知後 repaint；只讀取 Model 資料 |
| Controller | `SelectHandler` / `LinkHandler` / `CreateHandler` | 接收滑鼠事件；呼叫 Model 的 mutation API |

---

## 與現有設計的差異

| | 現在 | MVC 後 |
|-|------|--------|
| View 如何更新 | Controller 直接呼叫 `repaint()` | Model 呼叫 `notifyChanged()`，View 自動 repaint |
| 狀態在哪裡 | `CanvasModel`（無通知機制） | `UMLModel`（內建 Observer） |
| Controller 依賴 | 持有 `CanvasModel` reference | 持有 `UMLModel` reference，呼叫 mutation API |
