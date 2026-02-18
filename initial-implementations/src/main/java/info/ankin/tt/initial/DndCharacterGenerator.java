package info.ankin.tt.initial;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class DndCharacterGenerator {
    Random random = new SecureRandom();

    Integer rollDie(int dieSize) {
        return random.nextInt(1, dieSize + 1);
    }

    MultipleRolls<Integer> rollDice(int dieSize, int diceCount) {
        return new MultipleRolls<Integer>()
                .setOriginal(IntStream.range(0, diceCount).mapToObj(ignored -> rollDie(dieSize)).map(Roll::simple).toList())
                .sort();
    }

    MultipleRolls<MultipleRolls<Integer>> rollGroups(int dieSize, int diceCount, int numGroups) {
        return new MultipleRolls<MultipleRolls<Integer>>()
                .setOriginal(IntStream.range(0, numGroups).mapToObj(ignored -> rollDice(dieSize, diceCount)).map(Roll::multiple).toList())
                .sort();
    }

    public Map<Ability, Integer> genAbilityScores(TreeSet<Ability> abilityPriorities) {
        MultipleRolls<MultipleRolls<Integer>> mr = this.rollGroups(6, 4, 7);

        // drop each groups lowest
        for (Roll roll : mr.getSorted()) {
            ((Roll.MultipleRoll<?>) roll).getMr().dropLowest();
        }

        mr.resort();
        mr.dropLowest();
        var abScores = mr.getSorted().stream().map(Roll::value).toList();
        var abPriorities = abilityPriorities.stream().toList();

        return IntStream.range(0, abScores.size())
                .mapToObj(i -> Map.entry(abPriorities.get(i), abScores.get(i)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public DndCharacter generateCharacter(DndCharacterClass characterClass,
                                          DndCharacterType characterType,
                                          DndCharacterAlignment alignment,
                                          TreeSet<Ability> priorities) {
        return new DndCharacter()
                .setCharacterClass(characterClass)
                .setCharacterType(characterType)
                .setAlignment(alignment)
                .setLevel(1)
                .setAbilityScores(genAbilityScores(priorities))
                .setHitPoints(classToHitDieSize(characterClass))
                // .setArmorClass(null)
                ;
    }

    private int classToHitDieSize(DndCharacterClass characterClass) {
        return switch (characterClass) {
            case BARBARIAN -> 0;
            case BARD -> 0;
            case CLERIC -> 8;
            case DRUID -> 0;
            case FIGHTER -> 0;
            case MONK -> 0;
            case PALADIN -> 0;
            case RANGER -> 0;
            case ROGUE -> 0;
            case SORCERER -> 0;
            case WIZARD -> 0;
        };
    }

    @RequiredArgsConstructor
    @Getter
    enum Ability {
        STR("Strength"),
        DEX("Dexterity"),
        CON("Constitution"),
        INT("Intelligence"),
        WIS("Wisdom"),
        CHA("Charisma"),
        ;
        private final String abilityName;
    }

    // @formatter:off
    enum DndCharacterAlignment {
        LAWFUL_GOOD,    NEUTRAL_GOOD,    CHAOTIC_GOOD,
        LAWFUL_NEUTRAL, NEUTRAL_NEUTRAL, CHAOTIC_NEUTRAL,
        LAWFUL_EVIL,    NEUTRAL_EVIL,    CHAOTIC_EVIL,
    }
    // @formatter:on

    enum DndCharacterType {
        HUMAN, DWARF, ELF, GNOME, HALF_ELF, HALF_ORC, HALFLING,
    }

    @RequiredArgsConstructor
    @Getter
    enum DndCharacterClass {
        BARBARIAN("Bbn"),
        BARD("Brd"),
        CLERIC("Clr"),
        DRUID("Drd"),
        FIGHTER("Ftr"),
        MONK("Mnk"),
        PALADIN("Pal"),
        RANGER("Rgr"),
        ROGUE("Rog"),
        SORCERER("Sor"),
        WIZARD("Wiz"),
        ;
        private final String abbreviation;
    }

    sealed interface Roll extends Comparable<Roll> {
        static Roll simple(int value) {
            return new SimpleRoll(value);
        }

        static Roll multiple(MultipleRolls<?> mr) {
            return new MultipleRoll<>(mr);
        }

        int value();

        default int compareTo(Roll o) {
            return value() - o.value();
        }

        @Value
        @Accessors(fluent = true)
        class SimpleRoll implements Roll {
            int value;

            public String toString() {
                return String.valueOf(value);
            }
        }

        @Value
        class MultipleRoll<T extends Comparable<T>> implements Roll {
            MultipleRolls<T> mr;

            @Override
            public int value() {
                return mr.sum();
            }

            public String toString() {
                return String.valueOf(mr);
            }
        }
    }

    @Data
    @Accessors(chain = true)
    static class MultipleRolls<T extends Comparable<T>> implements Comparable<MultipleRolls<T>> {
        List<Roll> original;
        List<Roll> sorted;

        MultipleRolls<T> resort() {
            sorted = new ArrayList<>(sorted != null ? sorted : original);
            sorted.sort(Comparator.reverseOrder());
            return this;
        }

        MultipleRolls<T> sort() {
            if (sorted == null) {
                resort();
            }
            return this;
        }

        MultipleRolls<T> dropLowest() {
            sort().getSorted().removeLast();
            return this;
        }

        int sum() {
            return sort().getSorted().stream().mapToInt(Roll::value).sum();
        }

        @Override
        public int compareTo(MultipleRolls o) {
            return Integer.compare(this.sum(), o.sum());
        }
    }

    @Data
    @Accessors(chain = true)
    public static class DndCharacter {
        DndCharacterClass characterClass;
        DndCharacterType characterType;
        DndCharacterAlignment alignment;
        int level;
        Map<Ability, Integer> abilityScores;
        int hitPoints;
        int armorClass;
    }
}
