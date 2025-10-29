package info.ankin.tt.initial;

import info.ankin.tt.initial.DndCharacterGenerator.MultipleRolls;
import org.junit.jupiter.api.Test;

class DndCharacterGeneratorTest {

    DndCharacterGenerator generator = new DndCharacterGenerator();

    @Test
    void generateSeventh() {
        // var mr = generator.rollDice(2, 6);
        // System.out.println(mr);

        MultipleRolls<MultipleRolls<Integer>> mr = generator.rollGroups(6, 4, 7);

        // System.out.println(mr);

        // drop each groups lowest
        for (DndCharacterGenerator.Roll roll : mr.getSorted()) {
            ((DndCharacterGenerator.Roll.MultipleRoll<?>) roll).getMr().dropLowest();
        }

        mr.resort();
        mr.dropLowest();

        // System.out.println(mr);

        var abScores = mr.getSorted().stream().map(DndCharacterGenerator.Roll::value).toList();
        System.out.println(abScores);
    }

}
