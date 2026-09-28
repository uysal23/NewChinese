# LOCKED STEP 17 — Visual System

Unified 2.5D/3D CGI style, Character/Location Bible-driven. WebP preferred. Every final scene uses the locked four-layer visual-novel composition:

1. BG — background without characters
2. CHAR_A — transparent canonical character layer
3. CHAR_B — transparent canonical character layer
4. FG — transparent/subtle foreground depth layer

PREVIEW is a derived QA/control composite of the four runtime layers and is not a fifth runtime layer.

UI safe areas are mandatory. Active speaker may scale about 5–8%. A small speech bubble appears near the active character's mouth: dashed outline, white lightly opaque fill, no text, visually subtle but clear, synchronized to speakerId. Family-friendly forbidden-content rules are mandatory. Production is performed in 10-scene batches with continuity and quality validation.

## LOCKED pre-production grounding gate

No BG, CHAR_A, CHAR_B, FG or final PREVIEW may be accepted for a scene until both grounding stages below exist and are approved.

### A. Scene Context Grounding
For every scene, first derive and record:
- sceneId
- concise scene summary
- dialogue/communication purpose
- tone/mood
- main physical action
- characters present
- visible props required by the dialogue
- continuity from the previous scene
- continuity into the next scene
- visual notes that materially affect composition

The exact scene dialogue must be read before this grounding is approved.

### B. Location Matching
For every scene, then derive and record:
- sceneId
- locationId
- authoritative Location Bible reference
- why this location matches the scene dialogue
- dialogue evidence
- continuity evidence
- approvedForLayerPlanning = true/false

The location must be consistent with the dialogue, the scene visual manifest, the Location Bible, and adjacent-scene continuity.

## LOCKED layered visual planning gate

Only after Scene Context Grounding and Location Matching are approved may the scene receive a layer plan.

The layer plan must define:
- BG plan: empty location, camera, time/lighting, environmental continuity
- CHAR_A plan: canonical identity, canonical outfit, pose, gaze, gesture, prop interaction
- CHAR_B plan: canonical identity, canonical outfit, pose, gaze, gesture, prop interaction
- FG plan: subtle foreground props/depth that do not cover character bodies
- PREVIEW plan: expected merged composition for QA

## LOCKED production order

For each scene, production and QA follow this order:
1. BG
2. CHAR_A
3. CHAR_B
4. FG
5. PREVIEW

A PREVIEW is accepted only after the four required source layers exist.

## LOCKED asset validity rules

Every final asset must satisfy all of the following:
- One file = one asset. Never use a contact sheet, storyboard, collage, grid, multi-panel board, asset sheet, labeled production board or montage as a final scene asset.
- Final scene art contains no baked scene ID, asset name, panel label, prompt text, Chinese/Pinyin/Turkish subtitle, speech text, UI label or other readable production text.
- BG contains no scene characters.
- CHAR_A and CHAR_B are isolated transparent character cutouts. They must not contain a baked room/background, checkerboard pattern, text, border or unrelated prop layer.
- FG is an isolated transparent/subtle depth layer. It must never mask the characters' faces, torsos or speaking gestures.
- PREVIEW is a single 9:16 composite scene, not a collage or asset board.
- Composition remains mobile-safe and preserves subtitle/control safe areas.
- Character identity, age, face, hair, outfit and role continuity follow the Character Bibles. Canonical approved character layers may be reused when appropriate rather than generating identity drift.
- Location geometry, fixed objects and recognizable environmental identity follow the Location Bible.
- Props and action must match the actual dialogue context.
- Family-friendly/modest-clothing rules remain mandatory.
- No alcohol, bars/nightclubs, explicit content, erotic presentation or mini-skirts.
- No critical generated readable signage. Any required Chinese signage is a controlled app overlay, not baked into scene art.

## LOCKED completion gate

A scene may be marked visual complete only when:
- Scene Context Grounding is approved.
- Location Matching is approved.
- Layer Planning is approved.
- BG physically exists.
- CHAR_A physically exists.
- CHAR_B physically exists.
- FG physically exists.
- PREVIEW physically exists.
- Required filenames match the scene visual_manifest.
- Media status reports no missing visual assets.
- Validation passes.

If any one of these conditions fails, the scene remains pending.

## LOCKED 10-scene batch method

For each 10-scene batch:
1. Prepare Scene Context Grounding for all 10 scenes.
2. Prepare Location Matching for all 10 scenes.
3. Prepare layer plans for all 10 scenes.
4. Produce the four real layers and PREVIEW for each scene.
5. Import assets to their exact scene paths.
6. Run validation.
7. Only after validation, update batch acceptance/completion.

Do not silently skip or reorder these gates.

## Golden Demo V1 scene-rendering lock

Subsequent scenes use the accepted layered visual-novel composition. Background/location, foreground/depth layer and both character layers are combined in-app; character identity/outfit continuity follows the bibles. Active speaker emphasis is subtle (about 5–8%). Opaque foreground assets must never mask character bodies. Subtitle/control visibility takes priority over decorative layers.
