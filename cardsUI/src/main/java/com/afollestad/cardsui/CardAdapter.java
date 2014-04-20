package com.afollestad.cardsui;

import android.content.Context;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.afollestad.silk.adapters.SilkAdapter;

/**
 * A {@link SilkAdapter} that displays {@link Card} and {@link CardHeader} objects in a {@link CardListView}.
 *
 * @author Aidan Follestad (afollestad)
 */
public class CardAdapter<T extends CardBase> extends SilkAdapter<T> {

    public CardAdapter(Context context) {
        super(context);
        mAccentColor = context.getResources().getColor(android.R.color.black);
    }

    private int mAccentColor;
    private int mPopupMenu = -1;
    private Card.CardMenuListener mPopupListener;
    private boolean mCardsClickable = true;
    private int mLayout = R.layout.list_item_card;
    private int mLayoutNoContent = R.layout.list_item_card_nocontent;

    protected static final int TYPE_REGULAR = 0;
    protected static final int TYPE_NO_CONTENT = 1;
    protected static final int TYPE_HEADER = 2;

    /**
     * @deprecated Not supported for the card adapter.
     */
    @Override
    public boolean update(CardBase toUpdate, boolean addIfNotFound) {
        throw new IllegalAccessError("The CardAdapter does not currently support the update() method from the SilkAdapter.");
    }

    @Override
    public boolean isEnabled(int position) {
        CardBase item = getItem(position);
        if (!mCardsClickable && !item.isHeader()) return false;
        if (item.isHeader())
            return ((CardHeader) item).getActionCallback() != null;
        return item.isClickable();
    }

    /**
     * Sets the accent color used on card titles and header action buttons.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param color The resolved color to use as an accent.
     */
    public final CardAdapter setAccentColor(int color) {
        mAccentColor = color;
        return this;
    }

    /**
     * Sets the accent color resource used on card titles and header action buttons.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param colorRes The color resource ID to use as an accent.
     */
    public final CardAdapter setAccentColorRes(int colorRes) {
        setAccentColor(getContext().getResources().getColor(colorRes));
        return this;
    }

    /**
     * Sets a popup menu used for every card in the adapter, this will not override individual card popup menus.
     * You <b>should</b> call this method before adding any cards to the adapter to avoid issues.
     *
     * @param menuRes  The menu resource ID to use for the card's popup menu.
     * @param listener A listener invoked when an option in the popup menu is tapped by the user.
     */
    public final CardAdapter setPopupMenu(int menuRes, Card.CardMenuListener listener) {
        mPopupMenu = menuRes;
        mPopupListener = listener;
        return this;
    }

    /**
     * Sets whether or not cards in the adapter are clickable, setting it to false will turn card's list selectors off
     * and the list's OnItemClickListener will not be called. This <b>will</b> override individual isClickable values
     * set to {@link Card}s.
     */
    public final CardAdapter setCardsClickable(boolean clickable) {
        mCardsClickable = clickable;
        return this;
    }

    /**
     * Sets a custom layout to be used for all cards (not including headers) in the adapter. Must be called before
     * adding cards. This <b>does not</b> override layouts set to individual cards.
     */
    public final CardAdapter setCardLayout(int layoutRes) {
        mLayout = layoutRes;
        return this;
    }

    /**
     * Sets a custom layout to be used for all cards (not including headers) in the adapter with null content. Must be called before
     * adding cards. This <b>does not</b> override layouts set to individual cards.
     */
    public final CardAdapter setCardLayoutNoContent(int layoutRes) {
        mLayoutNoContent = layoutRes;
        return this;
    }

    @Override
    public int getLayout(int index, int type) {
        if (type == TYPE_HEADER)
            return R.layout.list_item_header;
        else if (type == TYPE_NO_CONTENT)
            return mLayoutNoContent;
        CardBase card = getItem(index);
        int layout = card.getLayout();
        if (layout == TYPE_REGULAR) layout = mLayout;
        return layout;
    }

    private void setupHeader(CardHeader header, View view) {
        TextView title = (TextView) view.findViewById(android.R.id.title);
        if (title == null)
            throw new RuntimeException("Your header layout must contain a TextView with the ID @android:id/title.");
        TextView subtitle = (TextView) view.findViewById(android.R.id.summary);
        if (subtitle == null)
            throw new RuntimeException("Your header layout must contain a TextView with the ID @android:id/summary.");
        title.setText(header.getTitle());
        if (header.getContent() != null && !header.getContent().trim().isEmpty()) {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(header.getContent());
        } else subtitle.setVisibility(View.GONE);
        TextView button = (TextView) view.findViewById(android.R.id.button1);
        if (button == null)
            throw new RuntimeException("The header layout must contain a TextView with the ID @android:id/button1.");
        if (header.getActionCallback() != null) {
            button.setVisibility(View.VISIBLE);
            button.setBackgroundColor(mAccentColor);
            String titleTxt = header.getActionTitle();
            if (header.getActionTitle() == null || header.getActionTitle().trim().isEmpty())
                titleTxt = getContext().getString(R.string.see_more);
            button.setText(titleTxt);
        } else button.setVisibility(View.GONE);
    }

    private void setupMenu(final CardBase card, final View view) {
        if (view == null)
            throw new RuntimeException("The card layout must contain a view with the ID @android:id/button1.");
        if (card.getPopupMenu() < 0) {
            // Menu for this card is disabled
            view.setVisibility(View.INVISIBLE);
            view.setOnClickListener(null);
            return;
        }
        int menuRes = mPopupMenu;
        if (card.getPopupMenu() != 0) menuRes = card.getPopupMenu();
        if (menuRes < 0) {
            // No menu for the adapter or the card
            view.setVisibility(View.INVISIBLE);
            view.setOnClickListener(null);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int menuRes = mPopupMenu;
                if (card.getPopupMenu() != 0) menuRes = card.getPopupMenu();
                Context themedContext = getContext();
                themedContext.setTheme(android.R.style.Theme_Holo_Light);
                PopupMenu popup = new PopupMenu(themedContext, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(menuRes, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (card.getPopupMenu() > 0 && card.getPopupListener() != null) {
                            // This individual card has it unique menu
                            card.getPopupListener().onMenuItemClick(card, item);
                        } else if (mPopupListener != null) {
                            // The card does not have a unique menu, use the adapter's default
                            mPopupListener.onMenuItemClick(card, item);
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    private void invalidatePadding(int index, View view) {
        int top = index == 0 ? R.dimen.card_outer_padding_firstlast : R.dimen.card_outer_padding_top;
        int bottom = index == (getCount() - 1) ? R.dimen.card_outer_padding_firstlast : R.dimen.card_outer_padding_top;
        view.setPadding(view.getPaddingLeft(),
                getContext().getResources().getDimensionPixelSize(top),
                view.getPaddingRight(),
                getContext().getResources().getDimensionPixelSize(bottom));
    }

    @Override
    public View onViewCreated(int index, View recycled, T item) {
        if (item.isHeader()) {
            final CardHeader header = (CardHeader) item;
            setupHeader(header, recycled);
            return recycled;
        }
        invalidatePadding(index, recycled);
        TextView title = (TextView) recycled.findViewById(android.R.id.title);
        if (title == null)
            throw new RuntimeException("The card layout must contain a TextView with the ID @android:id/title.");
        title.setText(item.getTitle());
        title.setTextColor(mAccentColor);
        TextView content = (TextView) recycled.findViewById(android.R.id.content);
        if (content != null) {
            if (!onProcessContent(content, item))
                content.setText(item.getContent());
        }
        setupMenu(item, recycled.findViewById(android.R.id.button1));
        ImageView icon = (ImageView) recycled.findViewById(android.R.id.icon);
        if (onProcessThumbnail(icon, item)) {
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }
        return recycled;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        CardBase item = getItem(position);
        if (item.isHeader()) return TYPE_HEADER;
        else if (item.getContent() == null || item.getContent().isEmpty()) return TYPE_NO_CONTENT;
        return TYPE_REGULAR;
    }

    protected boolean onProcessThumbnail(ImageView icon, T card) {
        if (icon == null)
            throw new RuntimeException("The card layout must contain an ImageView with the ID @android:id/icon.");
        if (card.getThumbnail() == null) return false;
        icon.setImageDrawable(card.getThumbnail());
        return true;
    }

    protected boolean onProcessContent(TextView content, T card) {
        // do nothing by default
        return false;
    }
}