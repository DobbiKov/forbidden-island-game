package View.SwingView;

import java.awt.*;

/**
 * A FlowLayout subclass that supports wrapping.
 *
 * This version guards against infinite sizes by
 * falling back to either the parent’s width or
 * the sum-of-components width when the target’s
 * own width is still zero.
 */
public class WrapLayout extends FlowLayout {
    public WrapLayout() {
        super();
    }
    public WrapLayout(int align) {
        super(align);
    }
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension min = layoutSize(target, false);
        // FlowLayout quirks: subtract hgap
        min.width -= (getHgap() + 1);
        return min;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int hgap   = getHgap();
            int vgap   = getVgap();

            // Determine the maximum width we should wrap at:
            int targetW = target.getWidth();
            if (targetW <= 0) {
                // if not yet laid out, try parent:
                Container p = target.getParent();
                if (p != null && p.getWidth() > 0) {
                    targetW = p.getWidth();
                }
            }
            // if still zero, just compute a single-row width:
            int wrapW;
            if (targetW > 0) {
                wrapW = targetW - (insets.left + insets.right + hgap*2);
            } else {
                // sum up component widths + gaps + insets
                int total = 0;
                for (Component comp : target.getComponents()) {
                    if (!comp.isVisible()) continue;
                    Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    total += d.width + hgap;
                }
                wrapW = total + insets.left + insets.right;
            }

            // now do the usual wrap algorithm
            int x = insets.left + hgap, y = insets.top + vgap;
            int rowH = 0;
            for (Component comp : target.getComponents()) {
                if (!comp.isVisible()) continue;
                Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                if (x + d.width > wrapW) {
                    // wrap to next line
                    x = insets.left + hgap;
                    y += rowH + vgap;
                    rowH = d.height;
                } else {
                    rowH = Math.max(rowH, d.height);
                }
                x += d.width + hgap;
            }
            y += rowH + vgap;           // add height of last row
            y += insets.bottom;         // bottom inset

            // total height is y, width can be whatever container is
            return new Dimension(
                    targetW > 0 ? targetW : wrapW + hgap,
                    y
            );
        }
    }
}
