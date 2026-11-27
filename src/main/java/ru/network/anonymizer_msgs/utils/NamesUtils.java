package ru.network.anonymizer_msgs.utils;


import ru.network.anonymizer_msgs.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class NamesUtils {


    private static final List<String> EMOJI_SET = List.of(
            "⭐", "🌟", "✨", "💫", "🌛", "🌜", "☀️", "🌤️",
            "🚗", "🚕", "🚙", "🏎️", "🚓", "🚑", "🚒", "🚐", "🚚", "🚛", "🛻", "🚲", "🛴", "🛵", "🏍️",
            "🚂", "🚝", "🚁", "✈️", "🚀", "🛸", "⛵", "🚤", "🛳️", "\uD83C\uDF08"
    );

    private static final List<String> EMOJI_MAN = List.of(
            "\uD83D\uDC71\u200D♂\uFE0F",
            "\uD83D\uDC68\u200D\uD83D\uDCBC",
            "\uD83D\uDC68\u200D\uD83D\uDD27",
            "\uD83D\uDE4B\u200D♂\uFE0F",
            "\uD83E\uDD35\u200D♂\uFE0F",
            "\uD83D\uDD7A",
            "\uD83E\uDD34"
    );

    private static final List<String> EMOJI_WOMAN = List.of(
            "\uD83D\uDC67",
            "\uD83D\uDC83",
            "\uD83D\uDC69",
            "\uD83D\uDC69\u200D⚖\uFE0F",
            "\uD83D\uDC78",
            "\uD83D\uDC70\u200D♀\uFE0F",
            "\uD83D\uDC81\u200D♀\uFE0F"
    );

    private static final List<String> WOMAN_NAMES = List.of(
            "Ama", "Anh", "Aoi", "Ava", "Aya",
            "Emi", "Eva", "Gia", "Ida", "Ivy",
            "Jia", "Kim", "Lea", "Lia", "Lin",
            "Liv", "Lou", "Mai", "Mei", "Mia",
            "Nao", "Nga", "Nia", "Noa", "Nyx",
            "Paz", "Rei", "Rin", "Sia", "Uma",
            "Xiu", "Yui", "Zoe", "Aiko", "Alba",
            "Anna", "Anya", "Aria", "Dina", "Diya",
            "Echo", "Elin", "Ella", "Elsa", "Emma",
            "Erin", "Hana", "Hina", "Ines", "Iris",
            "Isha", "Isla", "Jade", "Kaia", "Kira",
            "Lana", "Lani", "Lila", "Lily", "Lina",
            "Ling", "Linh", "Lior", "Lucy", "Luna",
            "Maja", "Mika", "Mila", "Mina", "Mira",
            "Nala", "Nari", "Nika", "Noor", "Nora",
            "Nour", "Nova", "Onyx", "Orla", "Rana",
            "Rina", "Rita", "Riya", "Rose", "Ruby",
            "Runa", "Safi", "Saga", "Sana", "Sena",
            "Skye", "Sora", "Tara", "Thao", "Tove",
            "Tuva", "Vega", "Vera", "Vika", "Wren",
            "Yara", "Ylva", "Yuki", "Yuna", "Zara",
            "Zora", "Zuri", "Aanya", "Aisha", "Alana",
            "Amara", "Amira", "Anela", "Ayana", "Carla",
            "Chloe", "Clara", "Farah", "Fiona", "Freja",
            "Freya", "Greta", "Hilde", "Katya", "Kavya",
            "Keira", "Layla", "Lucia", "Maeve", "Malia",
            "Manon", "Marta", "Moana", "Nadia", "Nadya",
            "Niamh", "Nisha", "Priya", "Rania", "Rowan",
            "Salma", "Sanaa", "Sofia", "Sonya", "Tessa");


    private static final List<String> MAN_NAMES = List.of(
            "Ace", "Ade", "Ali", "Anh", "Ash",
            "Ayo", "Ben", "Dae", "Dax", "Dev",
            "Duc", "Fox", "Huy", "Ian", "Jae",
            "Jax", "Jin", "Jun", "Kai", "Koa",
            "Lei", "Leo", "Max", "Nam", "Neo",
            "Ren", "Rio", "Ryo", "Tae", "Tom",
            "Wei", "Adam", "Alex", "Amir", "Ares",
            "Axel", "Bola", "Bran", "Carl", "Chen",
            "Cian", "Cole", "Crow", "Dean", "Dima",
            "Elio", "Emil", "Enzo", "Eric", "Erik",
            "Evan", "Femi", "Finn", "Haru", "Hawk",
            "Hiro", "Hugo", "Hyun", "Ivan", "Ivar",
            "Jack", "Jiho", "Joel", "Juan", "Kane",
            "Kimo", "Kito", "Knox", "Knut", "Kofi",
            "Kyle", "Lars", "Leif", "Leon", "Liam",
            "Loki", "Luca", "Luis", "Luka", "Luke",
            "Maks", "Manu", "Mark", "Mars", "Milo",
            "Minh", "Neil", "Nick", "Nico", "Nils",
            "Noah", "Odin", "Olaf", "Omar", "Onyx",
            "Owen", "Paul", "Rami", "Raul", "Ravi",
            "Remi", "Riku", "Rune", "Ryan", "Sami",
            "Sean", "Sefu", "Sora", "Sota", "Sung",
            "Sven", "Theo", "Thor", "Tore", "Vlad",
            "Wolf", "Yash", "Yuto", "Zane", "Zayn",
            "Zeus", "Zola", "Akira", "Aleki", "Andre");


    public static String getName(User user) {
        Boolean isMan = Optional.ofNullable(user)
                .map(User::getIsMan)
                .orElse(false);
        if (isMan) {
            return getRandom(EMOJI_SET) + getRandom(EMOJI_MAN) + "" + getRandom(MAN_NAMES);
        } else {
            return getRandom(EMOJI_SET) + getRandom(EMOJI_WOMAN) + "" + getRandom(WOMAN_NAMES);
        }
    }

    private static String getRandom(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(descriptions.size());
        return descriptions.get(index);
    }

}
