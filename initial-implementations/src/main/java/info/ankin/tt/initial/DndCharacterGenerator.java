package info.ankin.tt.initial;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
}
