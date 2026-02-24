package info.ankin.tt.initial;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static info.ankin.tt.initial.DndCharacterGenerator.Ability.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DndCharacterGeneratorTest {

    DndCharacterGenerator generator = new DndCharacterGenerator();

    @Test
    void generateSeventh() {
        Map<DndCharacterGenerator.Ability, Integer> abScores = generator.genAbilityScores(DndCharacterGenerator.SmallOrderedSet.of(List.of(INT, WIS, CHA, DEX, CON, STR)));
        System.out.println(abScores);
    }

    @Nested
    class DndCharacterTest {
        // page 8
        @ParameterizedTest
        @CsvSource({
                "1, -5",
                "2-3, -4",
                "4-5, -3",
                "6-7, -2",
                "8-9, -1",
                "10-11, 0",
                "12-13, 1",
                "14-15, 2",
                "16-17, 3",
                "18-19, 4",
                "20-21, 5",
                "22-23, 6",
                "24-25, 7",
                "26-27, 8",
                "28-29, 9",
                "30-31, 10",
                "32-33, 11",
                "34-35, 12",
                "36-37, 13",
                "38-39, 14",
                "40-41, 15",
                "42-43, 16",
                "44-45, 17",
        })
        void test_scoreToModifier(String range, int expected) {
            if (range.contains("-")) {
                String[] parts = range.split("-");
                assertThat(DndCharacterGenerator.DndCharacter.scoreToModifier(Integer.parseInt(parts[0])), is(expected));
                assertThat(DndCharacterGenerator.DndCharacter.scoreToModifier(Integer.parseInt(parts[1])), is(expected));
            } else {
                int input = Integer.parseInt(range);
                var output = DndCharacterGenerator.DndCharacter.scoreToModifier(input);
                assertThat(output, is(expected));
            }
        }
    }

}
