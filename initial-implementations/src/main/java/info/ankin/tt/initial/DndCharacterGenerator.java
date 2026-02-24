package info.ankin.tt.initial;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public Map<Ability, Integer> genAbilityScores(SmallOrderedSet<Ability> abilityPriorities) {
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
                                          SmallOrderedSet<Ability> priorities) {
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

    static int classToHitDieSize(DndCharacterClass characterClass) {
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

    static int classToBaseSave(DndCharacterClass characterClass, SavingThrow savingThrow) {
        //
        return switch (characterClass) {
            case BARBARIAN, FIGHTER, PALADIN, RANGER -> {
                yield 1;
            }
            case BARD, CLERIC, DRUID, MONK, ROGUE -> {
                yield 1;
            }
            case SORCERER, WIZARD -> {
                yield 1;
            }
        };
    }

    @RequiredArgsConstructor
    @Getter
    public enum Ability {
        STR("Strength"),
        DEX("Dexterity"),
        CON("Constitution"),
        INT("Intelligence"),
        WIS("Wisdom"),
        CHA("Charisma"),
        ;
        private final String abilityName;
    }

    @RequiredArgsConstructor
    @Getter
    public enum SavingThrow {
        FORTITUDE(Ability.CON),
        REFLEX(Ability.DEX),
        WILL(Ability.WIS),
        ;

        final Ability bonus;
    }

    public enum DndCharacterAlignment {
        LAWFUL_GOOD, NEUTRAL_GOOD, CHAOTIC_GOOD, LAWFUL_NEUTRAL, NEUTRAL_NEUTRAL, CHAOTIC_NEUTRAL, LAWFUL_EVIL, NEUTRAL_EVIL, CHAOTIC_EVIL,
    }

    public enum DndCharacterType {
        HUMAN, DWARF, ELF, GNOME, HALF_ELF, HALF_ORC, HALFLING,
    }

    @RequiredArgsConstructor
    @Getter
    public enum DndCharacterClass {
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

    @SuppressWarnings("NullableProblems")
    public static class SmallOrderedSet<T extends Comparable<T>> extends HashSet<T> {
        final PriorityQueue<T> pq = new PriorityQueue<>();

        @SuppressWarnings("unused")
        public SmallOrderedSet() {
        }

        public SmallOrderedSet(Collection<? extends T> c) {
            super(c.size());
            addAll(c);
        }

        public static <T extends Comparable<T>> SmallOrderedSet<T> of(Collection<? extends T> c) {
            return new SmallOrderedSet<>(c);
        }

        @Override
        public boolean add(T t) {
            boolean added = super.add(t);
            if (added)
                pq.add(t);
            return added;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = super.remove(o);
            if (removed)
                pq.remove(o);
            return removed;
        }

        @Override
        public Iterator<T> iterator() {
            return pq.iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return Spliterators.spliterator(iterator(), size(), Spliterator.SIZED | Spliterator.DISTINCT);
        }

        @Override
        public Object[] toArray() {
            return pq.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return pq.toArray(a);
        }

        @Override
        public Stream<T> stream() {
            return pq.stream();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            pq.forEach(action);
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
        List<Item> items;


        public int armorClass() {
            return 10 + armorClassFromItems(items) + scoreToModifier(abilityScores.get(Ability.DEX));
        }

        public Map<SavingThrow, Integer> savingThrows() {
            var result = new HashMap<SavingThrow, Integer>();
            for (SavingThrow savingThrow : SavingThrow.values()) {
                var total = class
            }

            return result;
        }

        static int armorClassFromItems(List<Item> items) {
            int result = 0;
            for (Item item : items) {
                if (item instanceof Item.Armor armorItem) {
                    result += armorItem.bonus();
                }
            }
            return result;
        }

        // visible for testing
        static int scoreToModifier(int integer) {
            return (integer / 2) - 5;
        }
    }

    public sealed interface Item {
        @RequiredArgsConstructor
        @Getter
        enum BaseBookArmor {
            // @formatter:off
            Padded(         new Armor(1, 8, 0, 5, 30, 20, 10, Armor.Weight.LIGHT)),
            Leather(        new Armor(2, 6, 0, 10, 30, 20, 15, Armor.Weight.LIGHT)),
            Studded_leather(new Armor(3, 5, -1, 15, 30, 20, 15, Armor.Weight.LIGHT)),
            chain_shirt(    new Armor(4, 4, -2, 20, 30, 20, 15, Armor.Weight.LIGHT)),
            hide(           new Armor(3, 4, -3, 20, 20, 15, 25, Armor.Weight.MEDIUM)),
            scale_mail(     new Armor(4, 3, -4, 25, 20, 15, 30, Armor.Weight.MEDIUM)),
            chainmail(      new Armor(5, 2, -5, 30, 20, 15, 40, Armor.Weight.MEDIUM)),
            breastplate(    new Armor(5, 3, -4, 25, 20, 15, 30, Armor.Weight.MEDIUM)),
            split_mail(     new Armor(6, 0, -7, 40, 20, 15, 45, Armor.Weight.HEAVY)),
            banded_mail(    new Armor(6, 1, -6, 35, 20, 15, 35, Armor.Weight.HEAVY)),
            half_plate(     new Armor(7, 0, -7, 40, 20, 15, 50, Armor.Weight.HEAVY)),
            full_plate(     new Armor(8, 1, -6, 35, 20, 15, 50, Armor.Weight.HEAVY)),
            // @formatter:on
            ;

            final Armor armor;
        }

        record Armor(
                int bonus,
                int maxDexBonus,
                int checkPenalty,
                int spellFailurePercent,
                int speed30,
                int speed20,
                int pounds,
                Weight weight
        ) implements Item {
            public enum Weight { LIGHT, MEDIUM, HEAVY, }
        }
    }
}
