package au.id.micolous.metrodroid.fragment;

import com.unnamed.b.atv.model.TreeNode;

import java.util.List;

import au.id.micolous.metrodroid.activity.AdvancedCardInfoActivity;
import au.id.micolous.metrodroid.card.Card;
import au.id.micolous.metrodroid.ui.ListItem;

public class CardRawDataFragment extends TreeListFragment {
    @Override
    protected List<ListItem> getItems() {
        Card card = Card.fromXml(getArguments().getString(AdvancedCardInfoActivity.EXTRA_CARD));
        return card.getRawData();
    }

    @Override
    public void onClick(TreeNode node, Object value) {

    }
}
