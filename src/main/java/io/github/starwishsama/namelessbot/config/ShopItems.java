package io.github.starwishsama.namelessbot.config;

import io.github.starwishsama.namelessbot.objects.Shop;
import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

@Data
public class ShopItems {
    public static Collection<Shop> shopItems = new HashSet<>();
}
