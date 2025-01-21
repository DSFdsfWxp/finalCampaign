package finalCampaign.ui;

import arc.struct.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import finalCampaign.bundle.*;
import mindustry.ctype.*;
import mindustry.ui.*;

public class contentSelecter extends Table {
    private ButtonGroup<ImageButton> group;
    private ObjectMap<ImageButton, UnlockableContent> map;
    private bundleNS bundleNS;
    private int count, col;
    private float size, iconSize;
    private Seq<Runnable> modifiedListener;

    public contentSelecter() {
        group = new ButtonGroup<>();
        map = new ObjectMap<>();
        bundleNS = new bundleNS("ui.contentSelecter");
        group.setMinCheckCount(0);
        count = 0;
        size = 46f;
        iconSize = 32f;
        col = 5;
        modifiedListener = new Seq<>();
    }

    public contentSelecter(float size, float iconSize, int col) {
        super();
        this.size = size;
        this.col = col;
        this.iconSize = iconSize;
    }

    public void size(float size) {
        this.size = size;
    }

    public void col(int col) {
        this.col = col;
    }

    public void minSelectedCount(int count) {
        group.setMinCheckCount(count);
    }

    public Cell<ImageButton> add(UnlockableContent content) {
        return add(new TextureRegionDrawable(content.uiIcon), null, content).tooltip(content.localizedName);
    }

    public Cell<ImageButton> add(Drawable image, Runnable clicked) {
        return add(image, clicked, null);
    }

    public Cell<ImageButton> add(Drawable image, String name) {
        return add(image, (Runnable) null, null).name(name).tooltip(bundleNS.get(name, name));
    }

    public Cell<ImageButton> add(Drawable image, String name, String tooltip) {
        Cell<ImageButton> res = add(image, (Runnable) null, null).name(name);
        if (!tooltip.isEmpty()) res.tooltip(tooltip);
        return res;
    }

    public Cell<ImageButton> add(Drawable image, @Nullable Runnable clicked, @Nullable UnlockableContent content) {
        Cell<ImageButton> cell = button(image, Styles.selecti,() -> {
            fireModified();
            if (clicked != null) clicked.run();
        }).size(size).scaling(Scaling.fit).group(group).name(content == null ? "null-" + count : content.getContentType().name() + "-" + content.name);
        ImageButton button = cell.get();
        button.resizeImage(iconSize);
        if (++ count % col == 0) row();
        if (content != null) map.put(button, content);
        return cell;
    }

    public void fireModified() {
        for (Runnable run : modifiedListener) run.run();
    }

    public void modified(Runnable run) {
        if (!modifiedListener.contains(run)) modifiedListener.add(run);
    }

    @Override
    public void clear() {
        count = 0;
        group.clear();
        map.clear();
        super.clear();
    }

    @Nullable
    public UnlockableContent getSelectedContent() {
        ImageButton checked = group.getChecked();
        return checked == null ? null : map.get(checked);
    }

    @Nullable
    public String getSelectedName() {
        ImageButton checked = group.getChecked();
        return checked == null ? null : checked.name;
    }

    public void setSelected(UnlockableContent content) {
        if (content == null) {
            group.uncheckAll();
            return;
        }
        ImageButton button = map.findKey(content, true);
        if (button != null) button.setChecked(true);
    }

    public void setSelected(String name) {
        if (name == null) {
            group.uncheckAll();
            return;
        }
        for (ImageButton button : group.getButtons()) if (button.name.equals(name)) button.setChecked(true);
    }
}
