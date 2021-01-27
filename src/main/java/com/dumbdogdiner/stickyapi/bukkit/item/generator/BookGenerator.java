/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.item.generator;

import com.dumbdogdiner.stickyapi.common.util.BookUtil;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.awt.print.Book;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Accessors(chain = true)
public class BookGenerator {
    private final JsonArray pages = new JsonArray();
    /** The generation of the book. */
    @Getter @Setter
    private @NonNull BookMeta.Generation generation = BookMeta.Generation.ORIGINAL;
    /** The material of the book. */
    @Getter
    private final Material bookType;
    /** The author of the book. Must not be null if the book is written. */
    @Getter @Setter
    private @Nullable String author;
    /** The title of the book, can be formatted using color codes. */
    @Getter @Setter
    private String title;


    private static final Gson G = new GsonBuilder().create();

    public BookGenerator(Material material) {
        if (material != Material.WRITABLE_BOOK && material != Material.WRITTEN_BOOK) {
            throw new IllegalArgumentException("Material must be WRITABLE_BOOK or WRITTEN_BOOK");
        }

        this.bookType = material;
    }

    /**
     * Add some pages to the book. May fail with {@link IllegalStateException} if the book is full or fills up.
     * @param pages Pages to add
     * @return This object, for chaining
     */
    public BookGenerator addPages(JsonObject... pages) {
        for (JsonObject page : pages) {
            addPage(page);
        }
        return this;
    }

    /**
     * Add a page to the book. May fail with {@link IllegalStateException} if the book is full.
     * @param page The page to add
     * @return This object, for chaining
     */
    public BookGenerator addPage(JsonObject page) {
        Preconditions.checkState(!isFull(), "Cannot add page, the book is overfilled!");
        pages.add(page);
        return this;
    }

    /**
     * Build a book from this generator.
     * @param qty Quantity of the item stack.
     * @return an {@link ItemStack} of the book, with pages and all other data
     */
    public ItemStack toItemStack(int qty) {
        Preconditions.checkArgument(qty > 0 && qty <= 16, "Invalid quantity specified, qty should be greater than 0 and less than or equal to 16, but was " + qty);
        Preconditions.checkState(pages.size() > 0, "Cannot generate book with no pages");
        Preconditions.checkState(pages.size() < BookUtil.PAGES_PER_BOOK, "Cannot generate book with an invalid number of pages (must be less than " + BookUtil.PAGES_PER_BOOK + ")");
        ItemStack stack = new ItemStack(bookType, qty);

        BookMeta meta = (BookMeta) stack.getItemMeta();

        if (bookType == Material.WRITTEN_BOOK) {
            meta.setTitle(title != null ? title : "");
            meta.setAuthor(author != null ? author : "");
            meta.setGeneration(generation);
        }

        stack = Bukkit.getUnsafe().modifyItemStack(stack, generateNbtString());

        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * @return The percentage of pages allowed that are used by this book.
     * TODO: Does not account for characters per page or packet size at this time.
     */
    public float percentFull() {
        return (float)pages.size() / (float)BookUtil.PAGES_PER_BOOK;
    }

    /**
     * TODO: Does not account for characters per page or packet size at this time.
     * @return True if the book is full.
     */
    public boolean isFull() {
        return percentFull() >= 1.0f;
    }


    /**
     * Uses a {@link StringJoiner} to convert pages JsonArray to the weird NBT list
     * @return {@link String} with NBT of the pages
     */
    @VisibleForTesting
    public String generateNbtString() {
        StringJoiner NBT = new StringJoiner("','", "{pages:['", "']}");
        pages.forEach(jsonElement -> NBT.add(G.toJson(jsonElement)/*.replace("'", "\\\"")*/));
        return NBT.toString().replace("\\n","\n").replace("\n","\\\\n");//TODO fix the new lines where they are originally
    }


}