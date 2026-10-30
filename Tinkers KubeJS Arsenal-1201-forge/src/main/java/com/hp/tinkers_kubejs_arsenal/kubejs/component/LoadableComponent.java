package com.hp.tinkers_kubejs_arsenal.kubejs.component;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.recipe.ItemMatch;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.Wrapper;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadable;

public final class LoadableComponent<T> implements RecipeComponent<T> {
    private final String type;
    private final Class<?> valueClass;
    private final Loadable<T> loadable;
    private final ComponentRole role;
    private final Function<DescriptionContext, TypeDescJS> description;
    private final Function<JsonElement, T> converter;
    private final Function<T, String> emptyCheck;
    private final Function<T, ItemStack> outputStack;

    public LoadableComponent(String type, Class<?> valueClass, Loadable<T> loadable, ComponentRole role, Function<DescriptionContext, TypeDescJS> description,
                             Function<JsonElement, T> converter, Function<T, String> emptyCheck, Function<T, ItemStack> outputStack) {
        this.type = type;
        this.valueClass = valueClass;
        this.loadable = loadable;
        this.role = role;
        this.description = description;
        this.converter = converter;
        this.emptyCheck = emptyCheck;
        this.outputStack = outputStack;
    }

    @Override
    public String componentType() {
        return this.type;
    }

    @Override
    public ComponentRole role() {
        return this.role;
    }

    @Override
    public Class<?> componentClass() {
        return this.valueClass;
    }

    @Override
    public TypeDescJS constructorDescription(DescriptionContext ctx) {
        return this.description.apply(ctx);
    }

    @Override
    public JsonElement write(RecipeJS recipe, T value) {
        return this.loadable.serialize(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read(RecipeJS recipe, Object from) {
        Object unwrapped = Wrapper.unwrapped(from);
        if (this.valueClass.isInstance(unwrapped)) {
            return (T) unwrapped;
        }
        JsonElement json = unwrapped instanceof JsonElement element ? element : JsonIO.of(unwrapped);
        if (json == null) {
            throw new IllegalArgumentException("Expected JSON value for " + this.type);
        }
        return this.converter.apply(json);
    }

    @Override
    public boolean hasPriority(RecipeJS recipe, Object from) {
        return this.valueClass.isInstance(Wrapper.unwrapped(from));
    }

    @Override
    public boolean isOutput(RecipeJS recipe, T value, ReplacementMatch match) {
        if (this.outputStack == null || value == null || !(match instanceof ItemMatch itemMatch)) {
            return false;
        }
        ItemStack stack = this.outputStack.apply(value);
        return stack != null && !stack.isEmpty() && itemMatch.contains(stack);
    }

    @Override
    public String checkEmpty(RecipeKey<T> key, T value) {
        return Objects.requireNonNull(this.emptyCheck.apply(value));
    }
}
