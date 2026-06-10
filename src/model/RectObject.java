package model;

import java.awt.Graphics2D;

public class RectObject extends BasicObject {

    private static final int[][] PORT_DIRS = {
            { Port.START,  Port.START  }, // 左上
            { Port.CENTER, Port.START  }, // 上中
            { Port.END,    Port.START  }, // 右上
            { Port.END,    Port.CENTER }, // 右中
            { Port.END,    Port.END    }, // 右下
            { Port.CENTER, Port.END    }, // 下中
            { Port.START,  Port.END    }, // 左下
            { Port.START,  Port.CENTER }, // 左中
    };

    public RectObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        initPorts(PORT_DIRS);
        updatePortPositions();
    }

    @Override
    protected void updatePortPositions() {
        applyPortPositions(PORT_DIRS);
    }

    @Override
    protected void drawShape(Graphics2D g) {
        g.fillRect(x, y, width, height);
    }

    @Override
    protected void drawShapeBorder(Graphics2D g) {
        g.drawRect(x, y, width, height);
    }
}
