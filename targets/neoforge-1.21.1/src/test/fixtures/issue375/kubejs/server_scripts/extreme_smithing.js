// Copy this fixture's kubejs directory into an isolated 1.21.1 server run directory.
// Exercise both startup and /reload with KubeJS (including 2101.7.2-build.368).
// The final PASS is emitted only after Minecraft has decoded the edited recipes.
const issue375Type = 'avaritia:extreme_smithing';
const issue375Array = 'issue375:array_input';
const issue375Single = 'issue375:single_input';
const issue375Created = 'issue375:created';
let issue375ScriptAssertionsPassed = false;

function issue375Assert(condition, message) {
    if (!condition) {
        throw new Error('[issue375] ' + message);
    }
}

function issue375Recipe(event, id) {
    let found = null;
    event.forEachRecipe({ id: id }, recipe => { found = recipe; });
    issue375Assert(found !== null, 'Missing original recipe ' + id);
    return found;
}

ServerEvents.recipes(event => {
    issue375ScriptAssertionsPassed = false;

    // Access typed values for every installed smithing recipe. Unknown-schema fallback
    // cannot satisfy these reads. This also checks real Avaritia/Delight recipes when installed.
    let existing = 0;
    event.forEachRecipe({ type: issue375Type }, recipe => {
        issue375Assert(recipe.get('template') !== null, recipe.getId() + ' template not parsed');
        issue375Assert(recipe.get('base') !== null, recipe.getId() + ' base not parsed');
        issue375Assert(recipe.get('addition') !== null, recipe.getId() + ' addition not parsed');
        issue375Assert(recipe.get('result') !== null, recipe.getId() + ' result not parsed');
        existing++;
    });
    issue375Assert(existing >= 2, 'Original datapack fixtures were not loaded');

    const array = issue375Recipe(event, issue375Array);
    issue375Assert(array.get('template').test(Item.of('avaritia:upgrade_smithing_template')), 'Template tag did not match');
    issue375Assert(!array.get('template').test(Item.of('minecraft:stick')), 'Template tag matched unrelated item');
    issue375Assert(array.get('base').test(Item.of('minecraft:diamond')), 'Single-item base did not match');
    issue375Assert(!array.get('base').test(Item.of('minecraft:emerald')), 'Single-item base matched unrelated item');
    ['minecraft:lava_bucket', 'minecraft:water_bucket', 'minecraft:powder_snow_bucket'].forEach(id => {
        issue375Assert(array.get('addition').test(Item.of(id)), 'Array addition lost ' + id);
    });
    issue375Assert(!array.get('addition').test(Item.of('minecraft:stick')), 'Array addition matched unrelated item');
    issue375Assert(array.get('result').is(Item.of('minecraft:diamond').item) && array.get('result').count === 2, 'Output stack lost id/count');

    const single = issue375Recipe(event, issue375Single);
    issue375Assert(single.get('template').test(Item.of('avaritia:upgrade_smithing_template')), '{item} template did not parse');
    issue375Assert(single.get('addition').test(Item.of('minecraft:stick')), '{item} addition did not parse');

    issue375Assert(event.countRecipes({ id: issue375Array, input: 'minecraft:water_bucket' }) === 1, 'Ingredient input search failed');
    issue375Assert(event.countRecipes({ id: issue375Array, output: 'minecraft:diamond' }) === 1, 'ItemStack output search failed');
    event.replaceInput({ id: issue375Array }, 'minecraft:diamond', '#issue375:bases');
    event.replaceOutput({ id: issue375Array }, 'minecraft:diamond', 'minecraft:emerald');
    issue375Assert(array.get('base').test(Item.of('minecraft:emerald')), 'replaceInput did not retain Ingredient/tag semantics');
    issue375Assert(!array.get('base').test(Item.of('minecraft:diamond')), 'replaceInput kept old base');
    issue375Assert(array.get('result').is(Item.of('minecraft:emerald').item), 'replaceOutput did not replace the ItemStack');
    array.result(Item.of('minecraft:emerald', 3));
    issue375Assert(array.get('result').count === 3, 'Generated result setter lost stack count');

    // Preserve the established positional constructor: result, template, base, addition.
    const created = event.recipes.avaritia.extreme_smithing(
        Item.of('minecraft:gold_ingot', 4),
        '#issue375:templates',
        'minecraft:diamond',
        ['minecraft:stick', 'minecraft:blaze_rod', 'minecraft:bone']
    ).id(issue375Created);
    issue375Assert(created.get('template').test(Item.of('avaritia:upgrade_smithing_template')), 'Constructor template order/type changed');
    issue375Assert(created.get('base').test(Item.of('minecraft:diamond')), 'Constructor base order/type changed');
    created.addition(['minecraft:flint', 'minecraft:feather', 'minecraft:string']);
    ['minecraft:flint', 'minecraft:feather', 'minecraft:string'].forEach(id => {
        issue375Assert(created.get('addition').test(Item.of(id)), 'Generated addition setter lost ' + id);
    });
    issue375Assert(!created.get('addition').test(Item.of('minecraft:stick')), 'Generated setter kept old addition');

    event.remove({ id: issue375Single, input: 'minecraft:stick', output: 'minecraft:iron_ingot' });
    issue375Assert(!event.containsRecipe({ id: issue375Single }), 'Filtered removal did not remove recipe');
    issue375ScriptAssertionsPassed = true;
});

ServerEvents.afterRecipes(event => {
    issue375Assert(issue375ScriptAssertionsPassed, 'Recipe script assertions did not complete');
    issue375Assert(event.countRecipes({ id: issue375Single }) === 0, 'Removed recipe reached Minecraft');
    issue375Assert(event.countRecipes({ id: issue375Array }) === 1, 'Modified recipe failed Minecraft decoding');
    issue375Assert(event.countRecipes({ id: issue375Created }) === 1, 'Created recipe failed Minecraft decoding');
    event.forEachRecipe({ id: issue375Array }, holder => {
        const recipe = holder.value();
        issue375Assert(recipe.template.test(Item.of('avaritia:upgrade_smithing_template')), 'Reload lost template tag');
        issue375Assert(recipe.base.test(Item.of('minecraft:emerald')), 'Reload lost modified base');
        issue375Assert(!recipe.base.test(Item.of('minecraft:diamond')), 'Reload restored old base');
        ['minecraft:lava_bucket', 'minecraft:water_bucket', 'minecraft:powder_snow_bucket'].forEach(id => {
            issue375Assert(recipe.additions.test(Item.of(id)), 'Reload lost array member ' + id);
        });
        issue375Assert(recipe.result.is(Item.of('minecraft:emerald').item) && recipe.result.count === 3, 'Reload lost modified output stack');
    });
    event.forEachRecipe({ id: issue375Created }, holder => {
        const recipe = holder.value();
        issue375Assert(recipe.template.test(Item.of('avaritia:upgrade_smithing_template')), 'Reload lost created template');
        issue375Assert(recipe.base.test(Item.of('minecraft:diamond')), 'Reload lost created base');
        ['minecraft:flint', 'minecraft:feather', 'minecraft:string'].forEach(id => {
            issue375Assert(recipe.additions.test(Item.of(id)), 'Reload lost created addition ' + id);
        });
        issue375Assert(recipe.result.is(Item.of('minecraft:gold_ingot').item) && recipe.result.count === 4, 'Reload lost created output stack');
    });
    console.info('[issue375] PASS: typed parsing, ingredient matching, find/modify/remove/create and final recipe decoding');
});
