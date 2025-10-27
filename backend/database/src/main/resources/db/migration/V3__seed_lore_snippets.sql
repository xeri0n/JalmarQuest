INSERT INTO lore_snippets (id, event_text, choice_options, consequences, conditions)
VALUES
    (
        'intro_ladybug_rescue',
        'A struggling ladybug clings to a bent blade of grass, its shell flecked with mud. Jalmar can intervene.',
        '["Help the ladybug","Observe quietly","Leave"]'::jsonb,
        '{
            "help": {
                "add_choice_tags": ["helped_ladybug_1"],
                "rewards": {"seeds": 10}
            },
            "observe": {
                "add_choice_tags": ["observed_ladybug_1"],
                "rewards": {"insight": 1}
            },
            "leave": {
                "add_choice_tags": ["ignored_ladybug_1"],
                "rewards": {}
            }
        }'::jsonb,
        '{
            "requires": []
        }'::jsonb
    ),
    (
        'followup_ladybug_matron',
        'Later, a Ladybug Matron arrives with a retinue, grateful for Jalmar''s earlier kindness.',
        '["Accept seeds","Recruit sentry","Decline"]'::jsonb,
        '{
            "accept": {
                "add_choice_tags": ["ladybug_matron_reward"],
                "rewards": {"seeds": 25}
            },
            "recruit": {
                "unlock_critter": "Ladybug Sentry",
                "rewards": {"nest_role": "sentry"}
            },
            "decline": {
                "add_choice_tags": ["humble_refusal_ladybug"],
                "rewards": {}
            }
        }'::jsonb,
        '{
            "requires": ["helped_ladybug_1"],
            "status_effects_missing": ["on_fire"]
        }'::jsonb
    ),
    (
        'wasp_patrol_ambush',
        'A wasp patrol buzzes overhead, their barbed shadows slicing the soil. They remember Jalmar''s past choices.',
        '["Stand your ground","Hide in the clover","Offer tribute"]'::jsonb,
        '{
            "stand": {
                "add_choice_tags": ["stood_against_wasp_patrol"],
                "rewards": {"status_effect": "Inspired"}
            },
            "hide": {
                "add_choice_tags": ["hid_from_wasp_patrol"],
                "rewards": {"status_effect": "Stealth"}
            },
            "tribute": {
                "add_choice_tags": ["paid_wasp_tribute"],
                "rewards": {"seeds": -15}
            }
        }'::jsonb,
        '{
            "requires": ["killed_wasp_1"],
            "status_effects_missing": []
        }'::jsonb
    )
ON CONFLICT (id) DO NOTHING;
