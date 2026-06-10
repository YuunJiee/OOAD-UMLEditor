# UML Editor — Use Case 時序圖

## Use Case A：建立形狀物件（Rect / Oval）

使用者從工具列選取 RECT 或 OVAL 模式，然後在畫布上拖曳來定義邊界框。

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Toolbar
    participant Canvas
    participant CreateHandler
    participant ShapeFactory
    participant CanvasModel

    User->>Toolbar: click RECT button
    Toolbar->>Canvas: setMode(Mode.RECT)

    User->>Canvas: mousePressed(x1, y1)
    Canvas->>CreateHandler: onPressed(x1, y1)
    CreateHandler-->>CreateHandler: pressX=x1, pressY=y1, drawing=true

    User->>Canvas: mouseDragged(x2, y2)
    Canvas->>CreateHandler: onDragged(x2, y2)
    CreateHandler-->>CreateHandler: currentX=x2, currentY=y2
    Canvas->>CreateHandler: drawPreview(g)
    CreateHandler->>ShapeFactory: drawPreview(g, x, y, w, h)

    User->>Canvas: mouseReleased(x2, y2)
    Canvas->>CreateHandler: onReleased(x2, y2)
    CreateHandler->>ShapeFactory: create(bx, by, bw, bh)
    ShapeFactory-->>CreateHandler: basicObject
    CreateHandler->>CanvasModel: add(basicObject)
    CreateHandler->>Canvas: onCreated.run()
    Canvas-->>Canvas: notifyModeChange(previousMode)
```

---

## Use Case B：建立連結（Association / Generalization / Composition）

### B（主流程）：成功建立連結

使用者選取連結模式，從一個形狀的 port 拖曳至另一個形狀的 port，成功建立連結。

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Toolbar
    participant Canvas
    participant LinkHandler
    participant CanvasModel
    participant LinkFactory

    User->>Toolbar: click ASSOCIATION button
    Toolbar->>Canvas: setMode(Mode.ASSOCIATION)

    User->>Canvas: mousePressed(x1, y1)
    Canvas->>LinkHandler: onPressed(x1, y1)
    LinkHandler->>CanvasModel: getPortOwnerByPort(x1, y1)
    CanvasModel-->>LinkHandler: fromObject
    LinkHandler->>fromObject: getPortAt(x1, y1)
    fromObject-->>LinkHandler: fromPort
    LinkHandler-->>LinkHandler: active = true

    User->>Canvas: mouseDragged(x2, y2)
    Canvas->>LinkHandler: onDragged(x2, y2)
    LinkHandler-->>LinkHandler: currentX=x2, currentY=y2
    Canvas->>LinkHandler: drawPreview(g)
    LinkHandler-->>Canvas: orange highlight on fromPort, gray preview line

    User->>Canvas: mouseReleased(x3, y3)
    Canvas->>LinkHandler: onReleased(x3, y3)
    LinkHandler->>CanvasModel: getPortOwnerByPort(x3, y3)
    CanvasModel-->>LinkHandler: toObject
    LinkHandler->>toObject: getPortAt(x3, y3)
    toObject-->>LinkHandler: toPort
    LinkHandler->>LinkFactory: create(fromObject, fromPort, toObject, toPort)
    LinkFactory-->>LinkHandler: link
    LinkHandler->>CanvasModel: addAtBottom(link)
```

### B.1：起點不在任何 port 上 → 取消

使用者在空白處或形狀本體（而非 port 附近）按下滑鼠，`getPortOwnerByPort` 回傳 null，立即結束，不進入拖曳流程。

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant LinkHandler
    participant CanvasModel

    User->>Canvas: mousePressed(x1, y1)
    Canvas->>LinkHandler: onPressed(x1, y1)
    LinkHandler->>CanvasModel: getPortOwnerByPort(x1, y1)
    CanvasModel-->>LinkHandler: null

    LinkHandler-->>LinkHandler: active = false, return
```

### B.2：放開時目標無效 → 取消

使用者放開滑鼠時，終點不在合法 port 上（目標物件為 null、與起點相同、或找不到 port），不建立連結。

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant LinkHandler
    participant CanvasModel

    Note over User,CanvasModel: active = true (B.1 did not trigger)

    User->>Canvas: mouseReleased(x3, y3)
    Canvas->>LinkHandler: onReleased(x3, y3)
    LinkHandler->>CanvasModel: getPortOwnerByPort(x3, y3)
    CanvasModel-->>LinkHandler: toObject (null, or same as fromObject, or toPort null)

    LinkHandler-->>LinkHandler: active = false, return (link cancelled)
```

---

## Use Case C：選取 / 取消選取物件

### C1：點擊選取單一物件

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant CanvasModel
    participant UMLObject

    User->>Canvas: mousePressed(x, y)
    Canvas->>SelectHandler: onPressed(x, y)
    SelectHandler->>CanvasModel: getTopmostNonLinkAt(x, y)
    CanvasModel-->>SelectHandler: topObj

    alt topObj not already selected
        SelectHandler->>CanvasModel: deselectAll()
        CanvasModel->>UMLObject: setSelected(false) [for each]
        SelectHandler->>topObj: setSelected(true)
        SelectHandler->>CanvasModel: bringToFront(topObj)
    end
    SelectHandler-->>SelectHandler: startMove()

    User->>Canvas: mouseReleased(x, y)
    Canvas->>SelectHandler: onReleased(x, y)
    SelectHandler-->>SelectHandler: state = NONE
```

### C2：點擊空白區域取消所有選取

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant CanvasModel

    User->>Canvas: mousePressed(x, y)
    Canvas->>SelectHandler: onPressed(x, y)
    SelectHandler->>CanvasModel: getTopmostNonLinkAt(x, y)
    CanvasModel-->>SelectHandler: null

    SelectHandler->>CanvasModel: deselectAll()
    SelectHandler-->>SelectHandler: state = RUBBER_BAND

    User->>Canvas: mouseReleased(x, y)
    Canvas->>SelectHandler: onReleased(x, y)
    SelectHandler-->>SelectHandler: finishRubberBand() — drag < threshold → no selection change
    SelectHandler-->>SelectHandler: state = NONE
```

### C3：橡皮筋框選多個物件

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant CanvasModel

    User->>Canvas: mousePressed(x1, y1)
    Canvas->>SelectHandler: onPressed(x1, y1)
    SelectHandler->>CanvasModel: deselectAll()
    SelectHandler-->>SelectHandler: state = RUBBER_BAND

    User->>Canvas: mouseDragged(x2, y2)
    Canvas->>SelectHandler: onDragged(x2, y2)
    Canvas->>SelectHandler: drawPreview(g)
    SelectHandler-->>Canvas: draw translucent blue rectangle

    User->>Canvas: mouseReleased(x2, y2)
    Canvas->>SelectHandler: onReleased(x2, y2)
    SelectHandler-->>SelectHandler: finishRubberBand(x2, y2)
    loop for each non-link UMLObject
        SelectHandler->>UMLObject: setSelected(selRect.contains(obj.getBounds()))
    end
    SelectHandler-->>SelectHandler: state = NONE
```

---

## Use Case D：群組 / 解散群組

### D1：將選取物件群組化

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant EditMenu
    participant Canvas
    participant CanvasModel
    participant CompositeObject

    User->>EditMenu: click Group menu item
    EditMenu->>Canvas: groupSelected()
    Canvas->>CanvasModel: groupSelected()

    CanvasModel-->>CanvasModel: collect selected non-link objects (≥ 2)
    CanvasModel-->>CanvasModel: deselect all groupable children
    CanvasModel->>CompositeObject: new CompositeObject(groupable)
    CanvasModel-->>CanvasModel: removeAll(groupable)
    CanvasModel-->>CanvasModel: deselectAll()
    CanvasModel-->>CanvasModel: add(composite)
    CanvasModel->>CompositeObject: setSelected(true)
    CanvasModel-->>Canvas: return true
    Canvas-->>Canvas: repaint()
```

### D2：解散選取的群組

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant EditMenu
    participant Canvas
    participant CanvasModel
    participant CompositeObject

    User->>EditMenu: click Ungroup menu item
    EditMenu->>Canvas: ungroupSelected()
    Canvas->>CanvasModel: ungroupSelected()

    CanvasModel-->>CanvasModel: verify exactly 1 selected CompositeObject
    CanvasModel-->>CanvasModel: idx = indexOf(composite)
    CanvasModel-->>CanvasModel: remove(composite)
    CanvasModel->>CompositeObject: getChildren()
    CompositeObject-->>CanvasModel: children list
    CanvasModel-->>CanvasModel: addAllAt(idx, children)
    CanvasModel-->>CanvasModel: deselectAll()
    CanvasModel-->>Canvas: return true
    Canvas-->>Canvas: repaint()
```

---

## Use Case E：移動物件

### E1：移動單一選取物件

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant CanvasModel
    participant BasicObject

    User->>Canvas: mousePressed(x1, y1) on object
    Canvas->>SelectHandler: onPressed(x1, y1)
    SelectHandler->>CanvasModel: getTopmostNonLinkAt(x1, y1)
    CanvasModel-->>SelectHandler: obj

    alt obj not already selected
        SelectHandler->>CanvasModel: deselectAll()
        SelectHandler->>obj: setSelected(true)
        SelectHandler->>CanvasModel: bringToFront(obj)
    end
    SelectHandler-->>SelectHandler: startMove(), lastDragX=x1, lastDragY=y1

    User->>Canvas: mouseDragged(x2, y2)
    Canvas->>SelectHandler: onDragged(x2, y2)
    SelectHandler-->>SelectHandler: dx = x2-lastDragX, dy = y2-lastDragY
    SelectHandler->>BasicObject: move(dx, dy)
    BasicObject-->>BasicObject: x+=dx, y+=dy, shift all ports
    SelectHandler-->>SelectHandler: lastDragX=x2, lastDragY=y2

    User->>Canvas: mouseReleased(x2, y2)
    Canvas->>SelectHandler: onReleased(x2, y2)
    SelectHandler-->>SelectHandler: state = NONE
```

### E2：同時移動多個選取物件

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant CanvasModel

    Note over User,CanvasModel: Multiple objects already selected via rubber-band

    User->>Canvas: mousePressed(x1, y1) on any selected object
    Canvas->>SelectHandler: onPressed(x1, y1)
    SelectHandler-->>SelectHandler: obj.isSelected() == true → skip deselectAll
    SelectHandler-->>SelectHandler: startMove()

    User->>Canvas: mouseDragged(x2, y2)
    Canvas->>SelectHandler: onDragged(x2, y2)
    loop for each selected UMLObject
        SelectHandler->>UMLObject: move(dx, dy)
    end
```

---

## Use Case F：縮放物件

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Canvas
    participant SelectHandler
    participant PortOwner
    participant Port

    Note over User,Port: Object is selected or hovered, ports are visible

    User->>Canvas: mouseMoved(px, py) near port
    Canvas->>SelectHandler: onMoved(px, py)
    SelectHandler->>PortOwner: getPortAt(px, py)
    PortOwner-->>SelectHandler: port (non-null)
    SelectHandler-->>Canvas: setCursor(CROSSHAIR)

    User->>Canvas: mousePressed(px, py) on port
    Canvas->>SelectHandler: onPressed(px, py)

    alt F.1 — object is CompositeObject (not a PortOwner, cannot resize)
        SelectHandler-->>SelectHandler: instanceof PortOwner check fails, skip resize
        SelectHandler-->>SelectHandler: falls through to move or rubber-band logic
    else object is BasicObject (implements PortOwner)
        SelectHandler->>PortOwner: getPortAt(px, py)
        PortOwner-->>SelectHandler: port
        SelectHandler-->>SelectHandler: startResize(portOwner, port)
        SelectHandler-->>SelectHandler: store resizeOrigX/Y/W/H
    end

    User->>Canvas: mouseDragged(mx, my)
    Canvas->>SelectHandler: onDragged(mx, my)
    SelectHandler-->>SelectHandler: applyResize(mx, my)
    SelectHandler->>Port: getDirection()
    Port-->>SelectHandler: [dx, dy]
    SelectHandler-->>SelectHandler: compute new x1,y1,x2,y2 based on direction
    SelectHandler->>PortOwner: resize(newX, newY, newW, newH)
    PortOwner-->>PortOwner: update bounds and ports

    User->>Canvas: mouseReleased(mx, my)
    Canvas->>SelectHandler: onReleased(mx, my)
    SelectHandler-->>SelectHandler: applyResize(mx, my) (final)
    SelectHandler-->>SelectHandler: state = NONE
```

---

## Use Case G：自訂標籤與樣式（編輯名稱與顏色）

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant EditMenu
    participant Canvas
    participant CanvasModel
    participant LabelDialog
    participant BasicObject
    participant Label

    Note over User,BasicObject: Exactly one BasicObject is selected

    User->>EditMenu: click Edit Label menu item
    EditMenu->>Canvas: editLabel()
    Canvas->>CanvasModel: getSelectedObjects()
    CanvasModel-->>Canvas: [basicObject]
    Canvas->>LabelDialog: new LabelDialog(window, basicObject)
    Canvas->>LabelDialog: setVisible(true)

    LabelDialog-->>User: show dialog with current name and color

    alt G.1 — User clicks Cancel (or presses Esc)
        LabelDialog-->>LabelDialog: dispose() without applying changes
    else User clicks OK (or presses Enter)
        User->>LabelDialog: type new name, pick new color, click OK
        LabelDialog->>BasicObject: setLabel(newName)
        BasicObject->>Label: setName(newName)
        LabelDialog->>BasicObject: setFillColor(newColor)
        BasicObject->>Label: setColor(newColor)
        LabelDialog-->>LabelDialog: dispose()

        Canvas-->>Canvas: repaint()
        Canvas->>BasicObject: draw(g, hovered)
        BasicObject->>Label: draw(g, x, y, width, height)
        Label-->>Canvas: render new name centered in shape
    end
```
