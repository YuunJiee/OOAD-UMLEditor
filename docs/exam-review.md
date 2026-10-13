# UML Editor — 期末考複習

---

## 一、整體架構（MVC）

> 🗣️ 口頭版：「整體是 MVC。CanvasModel 是 Model 負責資料；Canvas 是 View 加 Controller 負責畫面和接收事件；Handler 負責每個 Mode 的互動邏輯。Canvas 本身不處理細節，只負責把滑鼠事件轉發給對應的 Handler。」

```
View + Controller          Data Layer          Interaction Logic
Canvas                 →   CanvasModel    ←    MouseHandler
  └─ paintComponent()        └─ List<UMLObject>   ├─ SelectHandler
  └─ setMode()               └─ add/remove/query  ├─ LinkHandler
  └─ deleteSelected()                             └─ CreateHandler
```

- **CanvasModel**：管理所有物件的資料（增刪查改）
- **Canvas**：接收滑鼠事件，轉發給 Handler；負責重繪
- **Handler**：每種 Mode 的互動邏輯，不直接改畫面，只改 model

---

## 二、使用的設計模式

| 模式 | 在哪裡 | 說明 |
|---|---|---|
| Template Method | `BasicObject.draw()` | final 方法定義流程，abstract hook 給子類填入 |
| Template Method | `ArrowStyle.draw()` | 同上，繪製箭頭的流程 |
| Strategy | `ArrowStyle` 子類 | 箭頭形狀可替換（OpenArrow / HollowTriangle / Diamond） |
| Strategy | `Mode` enum BiPredicate | 每個 Mode 自己決定 Port 顯示邏輯 |
| Strategy | `MouseHandler` | 每個 Mode 對應不同的 Handler |
| Composite | `CompositeObject` | 群組本身也是 UMLObject，可以巢狀 |
| Factory Method | `LinkFactory`, `ShapeFactory` | Canvas 建立 Handler 時注入，Handler 不知道建立什麼 |
| State | `SelectHandler.State` | NONE / MOVING / RESIZING / RUBBER_BAND |
| Observer | `onModeChange Consumer<Mode>` | ToolPanel 和 Canvas 之間的 Mode 同步 |

---

## 三、類別繼承結構

```
UMLObject（abstract）
  ├─ BasicObject（abstract）  implements PortOwner, Groupable, AppearanceEditable
  │    ├─ RectObject
  │    └─ OvalObject
  ├─ CompositeObject           implements Groupable
  └─ LinkObject
```

- `UMLObject`：所有畫布物件的共同基底（selected, zIndex, draw）
- `BasicObject`：有形狀的物件（位置、大小、Port、Label、顏色）
- `CompositeObject`：群組，持有子物件 List，操作轉發給子物件
- `LinkObject`：連線，記住兩端的 PortOwner 和 Port

### 為何 UMLObject.contains() 和 move() 不是 abstract？
預設實作：`contains()` 回傳 false、`move()` 什麼都不做。
`LinkObject` 不可被點擊、位置由 Port 決定，不需要 override。

---

## 四、能力介面（Capability Interfaces）

| 介面 | 實作者 | 用途 |
|---|---|---|
| `PortOwner` | `BasicObject` | Link 的連接點，定義 getPortAt / getX / getY / resize |
| `Groupable` | `BasicObject`, `CompositeObject` | 可以被群組，CanvasModel 用此判斷 |
| `AppearanceEditable` | `BasicObject` | 可以編輯名稱和顏色，LabelDialog 用此介面 |

**為何要 Groupable？**
`BasicObject` 和 `CompositeObject` 都可群組，但繼承路徑不同。
有了 `Groupable`，`CanvasModel.groupSelected()` 只需要 `instanceof Groupable`，不用 `instanceof BasicObject || instanceof CompositeObject`。

---

## 五、Use Case 實作細節

### A. 建立物件

> 🗣️ 口頭版：「使用者拖曳時，CreateHandler 記住起點和終點座標，放開滑鼠後用 Math.min 和 Math.abs 計算出正確的邊界框，透過 ShapeFactory 建立物件加入 model，並自動切回前一個 Mode。」

**流程：**
1. `onPressed` → 記住起點座標
2. `onDragged` → 更新終點，`drawPreview` 顯示虛線輪廓
3. `onReleased` → 建立物件，呼叫 `onCreated` 回呼切回前一 Mode

**反向拖曳：**
```java
int bx = Math.min(pressX, x);   // 取較小 x 作為左上角
int bw = Math.abs(x - pressX);  // 寬度永遠正數
```
不管從哪個方向拖，都能正確計算出邊界框。

**切回前一 Mode：**
```java
// Canvas.setMode() 進入 RECT/OVAL 時記住 previousMode
handlers.put(Mode.RECT, new CreateHandler(model, () -> notifyModeChange(previousMode), rectFactory));
```

---

### B. 建立連線

> 🗣️ 口頭版：「按下時找起點的 PortOwner 和 Port，放開時找終點的 PortOwner 和 Port，兩端不能是同一個物件。確認都有找到後，透過 LinkFactory 建立對應的 LinkObject 加入 model 底層。」

**流程：**
1. `onPressed` → 找滑鼠位置的 `PortOwner`，記住 fromObject、fromPort
2. `onDragged` → 更新座標，`drawPreview` 畫預覽線 + Port highlight
3. `onReleased` → 找 toObject、toPort，不能和 fromObject 相同，建立 LinkObject

**Port 數量（規格書規定）：**
- `RectObject`：8 個 Port（四角 + 四邊中點）
- `OvalObject`：4 個 Port（上、下、左、右）

**Link 的 zIndex = 0：**
Link 用 `addAtBottom()` 加入，zIndex 預設 0，永遠畫在形狀下面不會蓋住物件。
> 🗣️ 口頭版：「Link 建立時是加到 list 最底層，zIndex 是 0，所以繪製時永遠在形狀下面，不會蓋住 Rect 或 Oval。」

**箭頭種類由 Factory 決定：**
```java
new LinkHandler(model, (f,fp,t,tp) -> new LinkObject(f,fp,t,tp, new OpenArrowStyle()))
```
`LinkHandler` 本身不知道要建立哪種箭頭。

**連線為何跟著物件移動？**
`LinkObject` 繪製時從 `fromPort.getPosition()` 到 `toPort.getPosition()`，Port 的位置跟著 BasicObject 移動而更新，所以線自然重新繪製。

---

### C. 選取物件

> 🗣️ 口頭版：「SelectHandler 有四個狀態：靜止、移動、縮放、框選。按下滑鼠時判斷按到的是 Port、物件還是空白，決定進入哪個狀態。滑鼠移動時偵測 hover，讓被 hover 的物件顯示 Port。」

**SelectHandler.State 狀態機：**

```
onPressed
  ├─ 按到 Port？         → RESIZING
  ├─ 按到 Groupable？    → 選取 → MOVING
  └─ 按到空白處？         → deselect all → RUBBER_BAND

onDragged
  ├─ MOVING    → 計算 dx/dy，move 所有選取物件
  ├─ RESIZING  → applyResize
  └─ RUBBER_BAND → 更新框選範圍

onReleased
  ├─ MOVING / RESIZING → 結束
  └─ RUBBER_BAND → finishRubberBand，選取完全在框內的物件
```

**Hover 偵測：**
`onMoved` 偵測滑鼠下方物件，更新 `hoveredObject`，觸發 repaint 顯示 Port。

**框選（Rubber Band）判斷條件：**
物件必須**完全落在**框選矩形內才算被選取，只是碰到邊不算。
> 🗣️ 口頭版：「框選時用物件的 getBounds 判斷，必須整個物件完全在框內才選取，不是只要碰到框就算。」

**點選物件後移到最上層（bringToFront）：**
```java
public void bringToFront(UMLObject obj) {
    if (objects.remove(obj)) {
        obj.setZIndex(99);     // 深度設最小（最上層）
        objects.add(obj);       // 加到 list 尾端（同 zIndex 時排後面的優先）
    }
}
```
`paintComponent` 依 zIndex 排序繪製，zIndex 最高的畫在最上面。

---

### D. 群組 / 取消群組

> 🗣️ 口頭版：「Group 是把選取的 Groupable 物件從 list 移除，包成一個 CompositeObject 再加回去。Ungroup 是把 CompositeObject 移除，子物件插回原本的位置。CompositeObject 的操作都是轉發給子物件，這是 Composite Pattern。」

**Group 實作：**
```java
// CanvasModel.groupSelected()
List<UMLObject> groupable = objects.stream()
    .filter(o -> o.isSelected() && o instanceof Groupable)
    .collect(...);
// 從 list 移除這些物件
objects.removeAll(groupable);
// 包成 CompositeObject 加回去
CompositeObject composite = new CompositeObject(groupable);
objects.add(composite);
composite.setSelected(true);
```

**Ungroup 實作：**
```java
// CanvasModel.ungroupSelected()
int idx = objects.indexOf(co);   // 記住原本的位置
objects.remove(co);
addAllAt(idx, co.getChildren()); // 子物件插回原位置
```
子物件插回原位置很重要，否則 zIndex 順序會亂。

**Composite Pattern 的效果：**
移動群組 = 呼叫 `CompositeObject.move()` → 轉發給每個子物件的 `move()`。

**巢狀群組：**
`CompositeObject` 實作 `Groupable`，所以群組可以再被群組，支援無限層巢狀。
> 🗣️ 口頭版：「CompositeObject 本身也實作了 Groupable，所以它可以和其他物件一起再被群組，形成巢狀群組，不需要特別處理。」

**刪除 CompositeObject 時 Link 也跟著刪：**
`CompositeObject.collectDeletingPortOwners()` 會遞迴進子物件，把所有 BasicObject 加入刪除集合，因此連到群組內任何子物件的 Link 都會一起被刪除。
> 🗣️ 口頭版：「刪除群組時，collectDeletingPortOwners 會遞迴進去把群組內所有 BasicObject 都收集起來，所以連到群組裡任何一個物件的 Link 也會跟著刪掉。」

---

### E. 移動物件

> 🗣️ 口頭版：「每次拖曳事件計算和上次的差值 dx、dy，對所有選取的物件呼叫 move。Link 不需要特別處理，因為它畫圖時直接讀 Port 的位置，Port 跟著物件移動，repaint 後 Link 自然在正確位置。」

**實作：**
```java
// SelectHandler MOVING 狀態
int dx = x - lastDragX;
int dy = y - lastDragY;
for (UMLObject obj : model.getSelectedObjects())
    obj.move(dx, dy);
lastDragX = x; lastDragY = y;
```

**連線為何自動跟著移動？**
不需要特別處理。Link 畫圖時直接讀 Port 的位置，Port 跟著 BasicObject 移動（`move()` 裡有 `port.shift(dx, dy)`），下一次 repaint 時 Link 自然在新位置。

---

### F. 調整物件大小（Resize）

> 🗣️ 口頭版：「拖曳 Port 時記住對角的固定座標，用 Math.min 和 Math.abs 計算新的位置和大小，支援反向拖曳。resize 時用 Math.max 強制最小 20px。Composite 物件因為不實作 PortOwner，沒有 Port 可以拖，所以無法 resize。」

**最小尺寸限制：**
```java
// BasicObject.resize()
this.width = Math.max(newW, MIN_SIZE);   // MIN_SIZE = 20
this.height = Math.max(newH, MIN_SIZE);
```

**反向拖曳（交叉縮放）：**
```java
// SelectHandler.applyResize()
// 根據拖曳的 Port 方向，用 Math.min / Math.abs 計算新的 x, y, w, h
int newX = Math.min(fixedX, mx);
int newW = Math.abs(mx - fixedX);
```
`fixedX` 是對角的座標，`mx` 是滑鼠位置，不管往哪邊拖，`Math.min` 永遠找左上角，`Math.abs` 永遠算出正寬高。

**Composite 不能 Resize：**
`SelectHandler` 只對 `PortOwner` 物件允許 resize。`CompositeObject` 沒有實作 `PortOwner`，所以不會觸發 RESIZING 狀態。

---

### G. 自定義 Label

> 🗣️ 口頭版：「確認選取的物件有實作 AppearanceEditable 介面後，開啟 LabelDialog，對話框讀取物件現有的名稱和顏色顯示出來。按 OK 就呼叫 setLabel 和 setFillColor 寫回物件，按 Cancel 直接關閉不做任何事。」

**流程：**
1. `Canvas.editLabel()` 確認選取的是 `AppearanceEditable`
2. 建立 `LabelDialog`，傳入 target
3. 對話框顯示現有名稱和顏色
4. OK → `target.setLabel()` + `target.setFillColor()`
5. Cancel → `dispose()`，不寫回

---

## 六、刪除的實作（兩步驟）

```java
// CanvasModel.deleteSelected()
// Step 1：收集要刪除的 PortOwner
Set<PortOwner> deleteOwners = new HashSet<>();
for (UMLObject obj : selected)
    obj.collectDeletingPortOwners(deleteOwners);  // BasicObject 把自己加進去

// Step 2：移除所有應刪除的物件（包含連到這些 PortOwner 的 Link）
objects.removeIf(o -> o.shouldBeDeleted(deleteOwners));
```

**LinkObject.shouldBeDeleted：**
```java
return isSelected() || deletingOwners.contains(fromObject) || deletingOwners.contains(toObject);
```
任何一端被刪除，Link 也跟著刪。

---

## 七、Port 顯示邏輯（Mode BiPredicate）

```java
// Mode enum
ASSOCIATION("...", (obj, hovered) -> obj instanceof PortOwner)
SELECT("...",      (obj, hovered) -> obj == hovered)
RECT("...",        (obj, hovered) -> false)
```

- **SELECT 模式**：只有 hovered 的物件顯示 Port
- **Link 模式**：所有 PortOwner 都顯示 Port（方便選擇連接點）
- **建立模式**：不顯示 Port

`Canvas.paintComponent()` 呼叫：
```java
obj.draw(g2, currentMode.shouldShowPortsFor(obj, hovered))
```

---

## 八、可能被問的問題

> 每題附「口頭回答版」，可以直接講出來不用找程式碼。

---

**Q: 怎麼實作 Group？**

> 🗣️ 口頭版：「我把所有被選取且實作 Groupable 介面的物件，從 list 移除，然後包成一個 CompositeObject 再加回去。CompositeObject 本身也是 UMLObject，所以畫布可以直接把它當一般物件操作。」

技術細節：`CanvasModel.groupSelected()` — 篩出 Groupable，removeAll，new CompositeObject，add 回去。

---

**Q: Resize 怎麼處理最小尺寸？**

> 🗣️ 口頭版：「在 BasicObject 的 resize 方法裡，寬高都會過一個 Math.max(newW, MIN_SIZE)，MIN_SIZE 是 20，所以不管怎麼拖，物件不會小於 20px。」

技術細節：`BasicObject.resize()` — `Math.max(newW, MIN_SIZE)`。

---

**Q: Resize 怎麼處理反向拖曳？**

> 🗣️ 口頭版：「我記住對角的固定座標，然後用 Math.min 取兩個座標中較小的當左上角，用 Math.abs 取差的絕對值當寬高，這樣不管往哪個方向拖，計算出來的 x、y、width、height 永遠都是正確的。」

技術細節：`SelectHandler.applyResize()` — `newX = Math.min(fixedX, mx)`，`newW = Math.abs(mx - fixedX)`。

---

**Q: 點選物件後怎麼跑到最上方？**

> 🗣️ 口頭版：「點選物件後我會呼叫 bringToFront，把它的 zIndex 設成 99 並移到 list 尾端。paintComponent 畫畫面時會依 zIndex 排序，zIndex 最高的最後畫，就會蓋在最上層。」

技術細節：`CanvasModel.bringToFront()` — setZIndex(99)，移到 list 尾端；`paintComponent` sorted by zIndex。

---

**Q: Link 為何跟著物件移動？**

> 🗣️ 口頭版：「Link 每次畫圖都是直接讀 Port 的當前位置，而 BasicObject 移動時 Port 也會跟著 shift。所以只要觸發 repaint，Link 自然就在新位置，完全不需要另外通知 Link。」

技術細節：`LinkObject.draw()` 讀 `fromPort.getPosition()`；`BasicObject.move()` 裡 `port.shift(dx, dy)`。

---

**Q: 為何要 Groupable 介面？**

> 🗣️ 口頭版：「BasicObject 和 CompositeObject 都可以被群組，但它們繼承路徑不同，沒有共同的父類可以代表『可群組』這件事。所以我用 Groupable 介面把它們統一，這樣判斷時只需要 instanceof Groupable，之後新增其他可群組的類型也不用改判斷邏輯。」

技術細節：OCP — 對擴充開放，對修改封閉。

---

**Q: PortOwner 的作用？**

> 🗣️ 口頭版：「PortOwner 是個介面，代表『這個物件有 Port，可以被 Link 連接』。LinkObject 只依賴 PortOwner 而不是 BasicObject，這樣 Link 不需要知道它連的是矩形還是橢圓，只需要知道對方有 Port 可以連就夠了。」

技術細節：ISP — LinkObject 的 fromObject / toObject 型別是 PortOwner，不是 BasicObject。

---

**Q: Template Method 在哪裡？**

> 🗣️ 口頭版：「BasicObject 的 draw 方法是 final，它定義了繪製的固定流程：先填色、再畫邊框、再畫 Label、最後畫 Port。裡面的 drawShape 和 drawShapeBorder 是 abstract，由 RectObject 和 OvalObject 各自填入，分別畫矩形和橢圓。」

技術細節：`BasicObject.draw()` final → 呼叫 abstract `drawShape()` / `drawShapeBorder()`。

---

**Q: Strategy Pattern 在哪裡？**

> 🗣️ 口頭版：「有三個地方。第一是 ArrowStyle，三種箭頭是三個策略可以互換。第二是 MouseHandler，每個 Mode 對應一個 Handler，Canvas 切換 Mode 就是換策略。第三是 Mode enum 裡的 BiPredicate，每個 Mode 自己帶著 Port 顯示的判斷邏輯。」

技術細節：ArrowStyle 子類、MouseHandler Map、Mode BiPredicate。

---

**Q: Composite Pattern 怎麼實作？**

> 🗣️ 口頭版：「CompositeObject 持有一個 List of UMLObject，它本身也繼承 UMLObject。所以外面不用區分它是群組還是單一物件，直接呼叫 move 或 draw，CompositeObject 就會把操作轉發給每一個子物件。而且子物件也可以是另一個 CompositeObject，自然支援巢狀群組。」

技術細節：`CompositeObject.move()` → `children.forEach(c -> c.move(dx, dy))`。

---

**Q: 刪除物件時 Link 怎麼一起刪？**

> 🗣️ 口頭版：「刪除分兩步。第一步收集所有被刪物件的 PortOwner 到一個 Set。第二步對整個 list 做 removeIf，LinkObject 的 shouldBeDeleted 會檢查自己的兩端是不是在那個 Set 裡，只要有一端被刪，Link 就跟著刪。」

技術細節：`collectDeletingPortOwners()` + `shouldBeDeleted()` + `removeIf()`。

---

**Q: 整個程式的架構是什麼？**

> 🗣️ 口頭版：「整體是 MVC。CanvasModel 是 Model，負責資料；Canvas 是 View 加 Controller，負責畫面和事件接收；Handler 是具體的互動邏輯，每個 Mode 一個 Handler，Canvas 只負責轉發事件，不處理細節。」

---

**Q: 為何 LinkObject 不用實作 contains() 和 move()？**

> 🗣️ 口頭版：「UMLObject 的 contains 和 move 有預設實作，contains 預設回傳 false，move 預設什麼都不做。LinkObject 本來就不能被點擊、也不需要自己移動，所以繼承預設就好，不用另外 override。」

---

**Q: 建立物件後為何自動切回 Select 模式？**

> 🗣️ 口頭版：「Canvas 建立 CreateHandler 時傳入了一個 onCreated callback，是一個 lambda，內容是切回 previousMode。每次進入 Rect 或 Oval 模式前，Canvas 會先記住當前模式是什麼，建立完物件後呼叫這個 callback 就自動切回去了。」
