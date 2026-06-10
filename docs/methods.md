# Method Behavior Descriptions

## UMLObject (abstract)

| Method | Behavior |
|--------|----------|
| draw(g, hovered) | abstract |
| contains(x, y) | abstract |
| getBounds() | abstract |
| move(dx, dy) | abstract |
| isSelected() | return selected |
| setSelected(bool) | selected = bool |
| getZIndex() | return zIndex |
| setZIndex(int) | zIndex = clamp(int, 0, 99) |
| collectDeletingPortOwners(set) | no-op; overridden by BasicObject and CompositeObject to populate the cascade-delete set |
| shouldBeDeleted(deletingOwners) | return selected |

## BasicObject (abstract, extends UMLObject)

| Method | Behavior |
|--------|----------|
| draw(g, hovered) | **final** (Template Method): fill shape with label color → draw black border → draw label centered → if selected or hovered, draw all ports |
| contains(px, py) | return px∈[x, x+width] && py∈[y, y+height] |
| getBounds() | return new Rectangle(x, y, width, height) |
| move(dx, dy) | x+=dx; y+=dy; for each port: port.shift(dx, dy) — keeps ports in sync without recalculation |
| resize(newX, newY, newW, newH) | update bounds (clamp to MIN_SIZE=20); updatePortPositions() |
| getPortAt(mx, my) | for each port: if port.contains(mx, my) return port; else return null |
| initPorts(dirs) | allocate ports[] array; create one Port per direction entry in dirs |
| updatePortPositions(dirs) | for each dir[dx,dy]: compute absolute position and call port.setPosition() |
| collectDeletingPortOwners(set) | set.add(this); marks this object so its connected links will also be removed |

## RectObject (extends BasicObject)

| Method | Behavior |
|--------|----------|
| RectObject(x, y, w, h) | initPorts(PORT_DIRS); updatePortPositions() — 8 ports at corners and edge midpoints |
| drawShape(g) | g.fillRect(x, y, width, height) |
| drawShapeBorder(g) | g.drawRect(x, y, width, height) |
| updatePortPositions() | updatePortPositions(PORT_DIRS) |

## OvalObject (extends BasicObject)

| Method | Behavior |
|--------|----------|
| OvalObject(x, y, w, h) | initPorts(PORT_DIRS); updatePortPositions() — 4 ports at top/right/bottom/left midpoints |
| contains(px, py) | ellipse hit test: ((px-cx)/rx)² + ((py-cy)/ry)² ≤ 1.05 (5% tolerance at border) |
| drawShape(g) | g.fillOval(x, y, width, height) |
| drawShapeBorder(g) | g.drawOval(x, y, width, height) |
| updatePortPositions() | updatePortPositions(PORT_DIRS) |

## CompositeObject (extends UMLObject)

| Method | Behavior |
|--------|----------|
| CompositeObject(children) | this.children = new ArrayList(children) |
| getBounds() | union of all children.getBounds() |
| contains(px, py) | getBounds().contains(px, py) |
| draw(g, hovered) | for each child: child.draw(g, false); if selected or hovered, draw blue dashed border with 4px padding |
| move(dx, dy) | for each child: child.move(dx, dy) |
| getChildren() | return Collections.unmodifiableList(children) — callers can iterate but not modify |
| collectDeletingPortOwners(set) | for each child: child.collectDeletingPortOwners(set) — recursively collects all BasicObject descendants |

## Port

| Method | Behavior |
|--------|----------|
| setPosition(x, y) | this.x=x; this.y=y — used after resize |
| shift(dx, dy) | x+=dx; y+=dy — used after move |
| contains(mx, my) | abs(x-mx) ≤ HIT_RADIUS && abs(y-my) ≤ HIT_RADIUS (square hit area, HIT_RADIUS=9) |
| draw(g) | g.fillRect centered on (x, y), size 8×8, black |
| getPosition() | return new Point(x, y) |
| getDirection() | return direction.clone() — [dx,dy]: 0=left/top edge, 1=center, 2=right/bottom edge |

## LinkObject (extends UMLObject)

| Method | Behavior |
|--------|----------|
| LinkObject(...) | store fromObject, fromPort, toObject, toPort, arrowStyle; set zIndex=0 (renders below shapes) |
| draw(g, hovered) | **final**: color=blue if hovered else black; stroke=1.5px; drawLinkShape(g, fromPoint, toPoint) |
| getFromPoint() | fromPort != null ? fromPort.getPosition() : new Point(fromObject.getX(), fromObject.getY()) |
| getToPoint() | toPort != null ? toPort.getPosition() : new Point(toObject.getX(), toObject.getY()) |
| drawLinkShape(g, p1, p2) | arrowStyle.draw(g, p1, p2) — delegates to Strategy |
| getBounds() | Rectangle from min(p1, p2) to max(p1, p2) |
| contains(px, py) | return false — links are not selectable by click |
| move(dx, dy) | no-op — links follow connected ports automatically via Port references |
| shouldBeDeleted(deletingOwners) | return isSelected() \|\| deletingOwners.contains(fromObject) \|\| deletingOwners.contains(toObject) |

## ArrowStyle (abstract) / OpenArrowStyle / HollowTriangleStyle / DiamondStyle

| Method | Behavior |
|--------|----------|
| draw(g, from, to) | **final** (Template Method): drawLine(from→to); g2=g.create(); translate+rotate to endpoint; if shouldFillWhite: fill white; draw arrowhead; g2.dispose() |
| getArrowHead() | abstract; subclass returns its cached Path2D shape |
| shouldFillWhite() | abstract; OpenArrowStyle: false; HollowTriangleStyle and DiamondStyle: true |

## Label

| Method | Behavior |
|--------|----------|
| draw(g, x, y, w, h) | if name not empty: measure string width via FontMetrics, draw centered in (x, y, w, h) |
| getName() / setName() | getter / setter for label text |
| getColor() / setColor() | getter / setter for fill color (shared with BasicObject's background) |

## MouseHandler (interface)

| Method | Behavior |
|--------|----------|
| onPressed(x, y) | abstract |
| onDragged(x, y) | abstract |
| onReleased(x, y) | abstract |
| onMoved(x, y) | default no-op |
| onActivate(mode) | default no-op; called when Canvas switches to this handler's mode |
| drawPreview(g) | abstract; called every repaint to render transient visual feedback |

## SelectHandler (implements MouseHandler)

Internal state machine: NONE → MOVING / RESIZING / RUBBER_BAND

| Method | Behavior |
|--------|----------|
| onPressed(x, y) | (1) check selected/hovered PortOwners for port hit → startResize; (2) else find topmost non-link at (x,y) → select + startMove; (3) else deselectAll + RUBBER_BAND |
| onDragged(x, y) | MOVING: dx=x-lastDragX; move all selected objects; update lastDrag. RESIZING: applyResize(x, y) |
| onReleased(x, y) | RUBBER_BAND: finishRubberBand; RESIZING: final applyResize; state=NONE |
| onMoved(x, y) | update hoveredObject (topmost non-link under cursor); setCursor(CROSSHAIR if on port / MOVE if on object / DEFAULT) |
| drawPreview(g) | if RUBBER_BAND and drag > 5px: fill translucent blue rect + draw dashed border |
| startResize(obj, port) | state=RESIZING; record resizingObject, resizingPort, original bounds (x,y,w,h) |
| startMove() | state=MOVING; lastDragX/Y = pressX/Y |
| finishRubberBand(mx, my) | build selRect from press→release; for each non-link: setSelected(selRect.contains(obj.getBounds())) — object must be fully within rect |
| applyResize(mx, my) | read port direction[]; dir==0 → move left/top edge; dir==2 → move right/bottom edge; call obj.resize(newX, newY, newW, newH) |

## LinkHandler (implements MouseHandler)

| Method | Behavior |
|--------|----------|
| onPressed(x, y) | fromObject=model.getPortOwnerByPort(x,y); if null: active=false, return; fromPort=fromObject.getPortAt(x,y); active=true |
| onDragged(x, y) | currentX=x; currentY=y |
| onReleased(x, y) | if !active: return; active=false; toObj=getPortOwnerByPort(x,y); if null or ==fromObject: return; toPort=toObj.getPortAt(x,y); if null: return; model.addAtBottom(factory.create(...)) |
| drawPreview(g) | draw orange highlight on fromPort; draw gray line to cursor; if cursor near valid target port: draw green highlight |

## CreateHandler (implements MouseHandler)

| Method | Behavior |
|--------|----------|
| onPressed(x, y) | pressX=x; pressY=y; drawing=true |
| onDragged(x, y) | currentX=x; currentY=y |
| onReleased(x, y) | compute bounding box (enforce MIN_SIZE); factory.create(bx,by,bw,bh); model.add(shape); onCreated.run() → Canvas reverts to previousMode |
| drawPreview(g) | draw dashed gray outline of shape being created (rect or oval) |

## CanvasModel

| Method | Behavior |
|--------|----------|
| add(obj) | objects.add(obj) — appended at end (top z-order) |
| addAtBottom(obj) | objects.add(0, obj) — used for links so they render below shapes |
| remove(obj) | objects.remove(obj) |
| addAllAt(index, objs) | objects.addAll(index, objs) — used by ungroup to restore children at original z-order |
| indexOf(obj) | return objects.indexOf(obj) |
| getAll() | return Collections.unmodifiableList(objects) |
| getSelectedObjects() | filter objects where isSelected() |
| getTopmostNonLinkAt(x, y) | find highest-zIndex non-link object that contains(x,y) |
| getPortOwnerByPort(mx, my) | find highest-zIndex PortOwner where getPortAt(mx,my) != null |
| bringToFront(obj) | remove obj; setZIndex(99); re-append at end of list |
| deselectAll() | for each obj: setSelected(false) |
| deleteSelected() | collect deletingOwners via collectDeletingPortOwners(); removeIf(o → o.shouldBeDeleted(deletingOwners)) — cascade removes connected links |
| groupSelected() | collect ≥2 selected non-link objects; deselect them; new CompositeObject(them); removeAll; add composite as selected |
| ungroupSelected() | if exactly 1 selected CompositeObject: record idx; remove it; addAllAt(idx, children); deselectAll |

## Canvas

| Method | Behavior |
|--------|----------|
| Canvas() | create SelectHandler, 3×LinkHandler, 2×CreateHandler; populate EnumMap; register mouse adapter + Delete key binding |
| setMode(mode) | save previousMode when switching to RECT/OVAL; currentMode=mode; handler.onActivate(mode); clearHover; update cursor |
| editLabel() | if exactly 1 BasicObject selected: open LabelDialog modal; repaint |
| paintComponent(g) | sort all objects by zIndex; links: draw with hovered=false; non-links: hovered=true if obj==hoveredObject or (isLinkMode && obj instanceof PortOwner); then activeHandler.drawPreview(g) |
| activeHandler() | return handlers.get(currentMode) — O(1) via EnumMap |
| notifyModeChange(mode) | currentMode=mode; reset cursor; fire onModeChange listener |

## EditMenu (extends JMenu)

| Method | Behavior |
|--------|----------|
| updateState(canvas) | Group: nonLinkCount≥2; Ungroup: exactly 1 CompositeObject selected; Label: exactly 1 BasicObject selected |
| createMenuItem(text, key, mask, listener) | new JMenuItem; if key≠0 set accelerator; add action listener |

## FileMenu (extends JMenu)

| Method | Behavior |
|--------|----------|
| FileMenu() | add Exit item → System.exit(0) |

## ToolPanel (extends JPanel)

| Method | Behavior |
|--------|----------|
| setActiveMode(mode) | highlight active button (black bg, white text); reset all others to default |
| createModeButton(mode) | new JButton(mode.label); on click: fire onModeChange callback |

## MainFrame (extends JFrame)

| Method | Behavior |
|--------|----------|
| MainFrame() | create ToolPanel, Canvas, statusBar; wire syncUI Consumer (ToolPanel ↔ Canvas ↔ statusBar); set size 75%×80% of screen |
| createMenuBar(canvas) | new JMenuBar with FileMenu and EditMenu(canvas) |

## LabelDialog (extends JDialog)

| Method | Behavior |
|--------|----------|
| LabelDialog(owner, target) | modal dialog; show current name (JTextField) and fill color (color button); OK → target.setLabel() + target.setFillColor() + dispose; Cancel/Esc → dispose without changes |
