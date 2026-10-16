package ru.network.anonymizer_msgs.utils;


import ru.network.anonymizer_msgs.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class NamesUtils {

    private static final List<String> ANIMAL_EMOJIS = List.of(
            "🐶", "🐱", "🐭", "🐰", "🦊", "🐻", "🐼", "🐯",
            "🦁", "🐮", "🐧", "🐤", "🐴", "🐝", "🐌", "🐢",
            "🐍", "🦎", "🦞", "🦀", "🐠", "🐬", "🦈", "🐊");

    private static final List<String> WOMAN_NAMES = List.of(
            "Nika", "Mira", "Lana", "Kira", "Zara",
            "Nora", "Luna", "Sia", "Aria", "Mika",
            "Vera", "Yuki", "Mei", "Layla", "Nala",
            "Zuri", "Freya", "Stella", "Rina", "Uma",
            "Emma", "Mia", "Lea", "Zoe", "Eva",
            "Ida", "Ava", "Ivy", "Elsa", "Ella",
            "Lily", "Lucy", "Ruby", "Rose", "Anna",
            "Clara", "Greta", "Chloe", "Manon", "Ines",
            "Jade", "Lou", "Alba", "Vega", "Paz",
            "Gia", "Lia", "Noa", "Sofia", "Marta",
            "Lucia", "Carla", "Rita", "Elin", "Astrid",
            "Freja", "Ingrid", "Sigrid", "Maja", "Saga",
            "Liv", "Tuva", "Ylva", "Hana", "Aoi",
            "Yuna", "Mina", "Sora", "Rin", "Nari",
            "Jia", "Ling", "Xiu", "Lin", "Kim",
            "Mai", "Anh", "Linh", "Nga", "Thao",
            "Trang", "Amira", "Noor", "Aya", "Dina",
            "Rana", "Lina", "Nia", "Ama", "Safi",
            "Lila", "Yasmin", "Farah", "Salma", "Nour",
            "Mariam", "Rania", "Tessa", "Vika", "Isla",
            "Kaia", "Lior", "Nyx", "Onyx", "Rei",
            "Skye", "Wren", "Nova", "Sena", "Yara");

    private static final List<String> MAN_NAMES = List.of(
            "Maks", "Leo", "Kai", "Max", "Nico",
            "Luca", "Omar", "Ren", "Jin", "Finn",
            "Hugo", "Milo", "Zane", "Ravi", "Kenji",
            "Diego", "Marco", "Erik", "Amir", "Neo",
            "Ivan", "Dima", "Theo", "Enzo", "Hiro",
            "Rami", "Kofi", "Yuto", "Atlas", "Raven",
            "Liam", "Noah", "Lucas", "Jack", "Owen",
            "Ryan", "Adam", "Eric", "Paul", "Mark",
            "Alex", "Nick", "Tom", "Ben", "Luke",
            "Sean", "Cole", "Dean", "Joel", "Kyle",
            "Evan", "Ian", "Carl", "Frank", "Mateo",
            "Pablo", "Remi", "Louis", "Jules", "Bruno",
            "Dario", "Elio", "Rafael", "Tomas", "Andre",
            "Sergio", "Matteo", "Pierre", "Henri", "Raoul",
            "Emil", "Cesar", "Pedro", "Juan", "Luis",
            "Miguel", "Carlos", "Raul", "Jorge", "Lars",
            "Bjorn", "Sven", "Nils", "Olaf", "Leif",
            "Rune", "Knut", "Ivar", "Magnus", "Anders",
            "Henrik", "Gustav", "Oscar", "Axel", "Viggo",
            "Sigge", "Tore", "Ryo", "Jun", "Minho",
            "Jiho", "Tae", "Chen", "Wei", "Lei",
            "Minh", "Duc", "Nam", "Huy", "Arjun",
            "Yuto", "Sora", "Haru", "Akira", "Takeshi",
            "Dae", "Sung", "Jae", "Hyun", "Anh",
            "Ali", "Karim", "Sami", "Nabil", "Tariq",
            "Malik", "Kwame", "Zola", "Tunde", "Sefu",
            "Jabari", "Ayo", "Chidi", "Emeka", "Kito",
            "Yusuf", "Idris", "Hamza", "Rashid", "Farid",
            "Nasser", "Ade", "Bola", "Femi", "Obinna",
            "Zeus", "Ares", "Helios", "Apollo", "Hermes",
            "Orion", "Perseus", "Leon", "Damian", "Xander",
            "Kostas", "Yannis", "Stavros", "Dimitri", "Alexis",
            "Petros", "Spiro", "Fox", "Wolf", "Ace",
            "Rex", "Knox", "Jax", "Kane", "Dax",
            "Ash", "Rio", "Onyx", "Loki", "Odin",
            "Thor", "Mars", "Blade", "Storm", "Hunter",
            "Falcon", "Hawk", "Ranger", "Ghost", "Shadow",
            "Crow", "Flint", "Steel");


    public static String getName(User user) {
        Boolean isMan = Optional.ofNullable(user)
                .map(User::getIsMan)
                .orElse(false);
        if (isMan) {
            return getRandom(ANIMAL_EMOJIS) + " " + getRandom(MAN_NAMES);
        } else {
            return getRandom(ANIMAL_EMOJIS) + " " + getRandom(WOMAN_NAMES);
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
