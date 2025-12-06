AvaritiaEvents.singularity(
    event => {
        event.register("avaritia:dragon_egg", s => {
            s.setDisplayName("singularity.avaritia.dragon_egg")
                .setColors(0x7a7b78, 0x7a7b78)
                .setCount(1)
                .setTimeCost(100)
                .setIngredient(Ingredient.of("minecraft:dragon_egg"))
        })
    })