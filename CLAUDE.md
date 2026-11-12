# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Projet

**Cobblemon BattlePhone** : le Battle Phone de Cobblemon Trainers, sorti en mod à part. Fabric et
NeoForge, Minecraft 1.21.1, Cobblemon 1.8.4, Kotlin (`matheo1712.battlephone`), pas de mixin.

Ce mod est un **client de Cobblemon Trainers**, qui est une API de dresseurs : tout ce que le
téléphone affiche - dresseurs, catégories, victoires, conditions, récompenses, lieux, skins,
équipes - vient de là. Ce dépôt ne possède que l'objet, son écran, et les paquets qui portent
un listing jusqu'à lui. Une règle sur un dresseur se change dans Cobblemon Trainers, jamais ici.

Le code, les commentaires et les logs sont en **anglais**. Tout texte affiché au joueur passe
par `assets/cobblemon_battlephone/lang/` via `BattlePhoneMod.lang` - jamais de littéral en dur.
Deux clés restent lues chez Cobblemon Trainers via `CobblemonTrainers.lang` :
`requirement.line` et `category.uncategorized`.

## Commandes

```bash
./gradlew build          # compile, teste, produit fabric/build/libs et neoforge/build/libs
./gradlew runClient      # client de dev Fabric, avec Cobblemon Trainers chargé à côté
```

Sur Windows, `.\gradlew.bat`. Java 21, imposé par les toolchains. Aucun JDK n'est sur le PATH de
cette machine : poser `JAVA_HOME` avant toute tâche Gradle.

## Identité

- **Un seul ID de mod, `cobblemon_battlephone`, sur les deux loaders.** NeoForge refuse le tiret,
  Fabric accepte le tiret bas : c'est la seule forme commune. Cobblemon Trainers, plus ancien que
  son portage NeoForge, vit avec deux (`cobblemon-trainers` / `cobblemon_trainers`).
- **Les objets gardent leurs ID `cobblemon-trainers:battle_phone*`**, ceux qu'ils avaient quand
  le téléphone faisait partie de Cobblemon Trainers : les téléphones déjà dans les inventaires
  survivent ainsi à la séparation, sans conversion. C'est le seul endroit où ce mod écrit sous le
  namespace d'un autre, et il en découle que ses **modèles et textures d'objet, ses recettes et
  leurs advancements** vivent sous `assets/cobblemon-trainers/` et `data/cobblemon-trainers/`
  dans ce jar. Les noms de clés de ces objets (`item.cobblemon-trainers.battle_phone*`) en
  découlent aussi, et sont dans notre fichier de langue. Cobblemon Trainers refuse de son côté
  tout `battle_phone` dans son propre jar, pour qu'aucun objet ne soit enregistré deux fois.
- **Tout le reste est sous `cobblemon_battlephone`** : paquets réseau, clés de traduction,
  textures d'interface (`textures/gui/battle_phone/`), icône.

## Architecture

- **`BattlePhoneMod`** - l'ID, le logger, `id()` et `lang()`.
- **`BattlePhone`** - l'init commune : objets, paquets, place dans l'onglet créatif de Cobblemon
  Trainers (`TrainerBlocks.addToTab`).
- **`BattlePhoneItems` / `BattlePhoneItem`** - les sept couleurs, et l'ouverture de l'écran.
- **`BattlePhoneNetworking`** - le listing, la demande d'équipe et sa réponse, l'appel d'un
  dresseur.
- **`client.BattlePhoneClient`** - l'entrypoint client : l'écran et le cache d'équipes.
- **`client.BattlePhoneScreen` / `client.TrainerTeamCache`** - l'écran et les équipes reçues.
- **`fabric.BattlePhoneFabric` / `neoforge.BattlePhoneNeoForge`** - les entrypoints. Ils ne
  décident que du moment où l'init commune tourne.

Les skins ne passent pas par ici : ils viennent du canal de Cobblemon Trainers
(`TrainerSkinNetworking`, lu côté client dans `TrainerSkinCache`), que l'écran de versus lit
aussi.

### La dépendance à Cobblemon Trainers

- **Elle est prise sur le Maven Modrinth**,
  `maven.modrinth:cobblemon-trainers-rerebleue:<version>-fabric` et `-neoforge` : les numéros de
  version Modrinth de Cobblemon Trainers portent leur loader en suffixe, et
  `cobblemon_trainers_version` est la partie commune. On vise donc ce qui est réellement publié -
  un changement d'API ne se voit ici qu'après une release là-bas. Pour essayer une API pas encore
  publiée, un `publishToMavenLocal` là-bas et un `mavenLocal()` ici, le temps de l'essai.
- **`cobblemon_trainers_version` est aussi la version minimale déclarée** dans
  `fabric.mod.json` et `neoforge.mods.toml`, et elle garde son étiquette de pré-version :
  en semver, `2.0.0-alpha` vient *avant* `2.0.0`, donc `>=2.0.0` refuserait l'alpha. Rien en
  dessous de `2.0.0-alpha`, la première sans le téléphone : à côté d'une plus ancienne, les deux
  enregistreraient les objets `battle_phone` et le jeu ne démarrerait pas.
- **`common` compile contre le jar Fabric**, remappé en noms Mojang par Loom
  (`modCompileOnly`) ; NeoForge recompile les mêmes sources contre le jar NeoForge. Un
  `modCompileOnly` n'atteint pas le classpath des tests : le jar remappé y est repris du
  classpath de compilation, et c'est pourquoi le filtre porte sur son nom.
- **Rien d'`internal` chez Cobblemon Trainers n'est visible ici.** Une classe dont on a besoin
  doit y être publique - `TrainerTeamCodec` l'a été pour ça.
- **On passe par sa plateforme** (`Platform`, `ClientPlatform`) pour les registres et les
  paquets, ce qui garde `common` identique sur les deux loaders. Sous NeoForge, elle met tout en
  file et le donne au registre depuis son propre listener ; d'où l'init lancée pendant le
  `RegisterEvent` des blocs et l'`ordering = "AFTER"` dans `neoforge.mods.toml` - le commentaire
  de `BattlePhoneNeoForge` dit pourquoi l'ordre tient.

## L'écran et ses règles

Ce qui suit vient tel quel du `CLAUDE.md` de Cobblemon Trainers, au moment où le téléphone en
est sorti. Les classes de l'API qu'il nomme - `TrainerRewards`, `TrainerRegistry`,
`TrainerProgress`, `ShowdownTeamParser` - sont là-bas.


`cobblemon-trainers:battle_phone` ouvre le suivi de progression : les dresseurs `listed`, un
onglet par datapack, un intertitre par catégorie, et le skin de chacun. C'est le pendant
joueur de `/cobblemontrainers list`, qui reste opérateur - l'objet ne donne aucun pouvoir, il lit.

**Le craft du Battle Phone** est `data/cobblemon-trainers/recipe/battle_phone.json` : quatre
lingots de fer aux coins, quatre de cuivre sur les côtés, une noigrume bleue au centre. Trois
choses à savoir avant d'y toucher :

- **Le dossier est `recipe/`, au singulier**, comme `advancement/` depuis 1.21.
- **Les ingrédients sont des items, pas des tags.** `c:ingots/iron` marcherait - Cobblemon
  l'emploie - mais un tag vide échoue *en silence* : la recette disparaît sans un mot dans le
  log. Deux lingots vanilla ne valent pas ce risque.
- **La noigrume vient de Cobblemon** (`cobblemon:blue_apricorn`), qui est une dépendance dure :
  la recette ne peut donc pas se retrouver sans son ingrédient.

Points à ne pas redécouvrir :

- **L'onglet, c'est le namespace.** Un « datapack » n'existe pas à l'exécution : ce que le mod
  connaît d'un dresseur, c'est l'ID que lui a donné le pack qui l'a fourni. Le regroupement se
  fait donc sur le namespace, côté client, sur une liste que le serveur envoie déjà dans
  l'ordre d'affichage. L'onglet « tous les datapacks » n'apparaît qu'à partir de deux
  namespaces, sinon il doublerait le seul autre.
- **Le client ne regroupe jamais, il découpe.** Les intertitres de catégorie sont les runs de
  lignes voisines qui portent le même `category` : c'est le serveur qui a trié, et refaire un
  `groupBy` ici pourrait contredire son ordre. Un pack dont tous les dresseurs sont à la racine
  n'a aucun intertitre - un titre unique serait une ligne dépensée pour rien.
- **Le listing vient du serveur**, pour les mêmes raisons que l'écran du bloc : le client n'a
  ni `TrainerRegistry` ni `TrainerProgress`. Pas de `MenuType` non plus, il n'y a pas
  d'inventaire.
- **Les skins ne sont pas dans le listing.** Chacun pèse quelques kilo-octets et un monde peut
  compter cent dresseurs : ils sont demandés un par un, quand une ligne en a besoin, et servis
  depuis le cache de `TrainerSkins`. `TrainerSkinCache.get` a donc le droit de répondre null,
  ce qui veut dire « demandé, pas encore arrivé ».
- **Le serveur répond toujours, même sans image.** Une absence de réponse laisserait le client
  attendre indéfiniment, et il redemanderait à chaque frame. Un skin irrésolu est donc une
  entrée de cache sans texture.
- **L'ID demandé est revalidé** contre `TrainerRegistry`, son `listed` et son verrou : la
  demande vient d'un client, donc rien ne garantit qu'il renvoie un des dresseurs qu'on lui a
  proposés - et un dresseur caché n'a jamais été proposé.
- **Les skins sont dessinés à plat**, face par face depuis l'image (`TrainerSkinRenderer`), et
  non par le renderer d'entité : un dresseur jamais rencontré n'a pas d'entité à afficher.
  C'est aussi ce qui garde le mod sans renderer.
- **`GuiGraphics.blit` laisse l'état de blending à son appelant**, et dessiner du texte
  termine son propre batch - ce qui *désactive* le blending au passage. Un `enableBlend()` en
  début de `render()` ne suffit donc pas : c'est chaque blit qui l'active, d'où le helper
  `blit` de l'écran. Sans ça, le cadre effacerait ce qu'il recouvre.
- **`ShowdownTeamParser.countPokemon` existe pour cet écran.** Compter l'équipe avec `parse`
  ferait passer chaque espèce, capacité et objet de chaque dresseur par les registres de
  Cobblemon à chaque ouverture.
- **Les textures sont les nôtres**, sous
  `assets/cobblemon-trainers/textures/gui/battle_phone/`, volontairement neutres : un cadre,
  une case, deux flèches, un marqueur. Elles ne dépendent pas des assets de Cobblemon - une
  version antérieure les empruntait au Pokédex, ne pas y revenir. Le trou transparent de
  `frame.png` doit correspondre aux constantes `INNER_*` de l'écran : c'est lui qui délimite la
  zone dessinée.
- **Les textures sont libérées à la déconnexion** (`ClientPlayConnectionEvents.DISCONNECT`) :
  un autre serveur peut avoir d'autres dresseurs sous les mêmes ID.
- **L'équipe n'est envoyée qu'après la victoire.** Le serveur revérifie `TrainerProgress`
  avant de répondre : la fiche est une récompense, pas un outil pour repérer le prochain
  combat. Un dresseur pas encore battu reçoit une réponse *vide*, pas une absence de réponse,
  pour la même raison que les skins.
- **L'équipe passe par `create()`**, comme à l'apparition d'un dresseur, parce que c'est la
  seule façon de connaître les **aspects** finaux d'un Pokémon : le parseur pose une forme
  dans la chaîne de propriétés, et seul `create()` la transforme en aspects - ceux qui
  choisissent le modèle affiché (forme régionale, chromatique). D'où
  `ShowdownTeamParser.countPokemon` pour le listing, qui lui n'a besoin que du nombre.
- **Les modèles sont rendus par `drawProfilePokemon`** (`com.cobblemon.mod.common.client.gui`),
  avec un `FloatingState` par case, jeté quand la sélection change : un état appartient au
  modèle pour lequel il a été posé. C'est lui qui met un Wailord et un Joltik à la même échelle
  utile, et **l'appel ne nomme que les paramètres qu'il veut** - ce qui est la seule raison pour
  laquelle le passage à Cobblemon 1.8 n'a rien cassé ici. Sa signature a bougé : le booléen
  `applyProfileTransform` est devenu un `ProfileTransformType` (`PROFILE`, `NONE`, `POKEDEX`,
  `SUMMARY`, pour les `profilePokedexScale` et compagnie des posers), et un `int` de lumière a
  été ajouté en fin de liste. Les deux sont laissés à leur défaut, et `PROFILE` est exactement
  ce que valait l'ancien `true` - vérifié dans le bytecode des deux versions. Passer l'un des
  deux positionnellement casserait au prochain paramètre ajouté.
- **La taille tient en deux facteurs, pas un.** Le `scale` de `drawProfilePokemon` est un
  `matrixStack.scale` brut : seul, il rend un Pokémon haut de quelques pixels. Cobblemon met un
  `scale(2.5f, 2.5f, 1f)` sur la pose *avant* l'appel et passe `4.5f` - c'est ce couple qui est
  repris ici, y compris son écrasement en profondeur, le `2.5` étant le seul des deux à porter
  sur z. Ne pas les fusionner en un seul nombre.
- **Le modèle descend depuis le point de translation**, il ne se tient pas dessus : viser le
  haut de la case, pas le bas. Un modèle occupe à peu près la hauteur d'une case de la grille.
- **L'ID du dresseur n'est plus affiché** dans la fiche, volontairement : c'est le namespace
  qui compte pour le joueur, et il est déjà dans le sélecteur et dans les en-têtes de la
  liste. Ne pas le remettre.
- **Les lignes de la liste n'ont pas toutes la même hauteur** - un en-tête de datapack est
  plus court qu'un dresseur -, donc l'affichage, le clic et le calcul du défilement parcourent
  les lignes au lieu de diviser par une hauteur. Les en-têtes n'existent que dans l'onglet
  « tous les datapacks » ; ailleurs le sélecteur nomme déjà le pack.
- **Le bouton d'appel est dessiné en rectangles**, pas en texture, comme tout ce qui n'est pas
  l'une des six images. Il est logé au bout de la bande du statut : c'est la seule zone libre
  qui reste en haut, l'équipe prenant tout le dessus et le cadre commençant quatre pixels sous
  la ligne de statut. Voir « Appeler un dresseur ».
- **Les récompenses tiennent dans un rail à côté du dresseur**, une case par objet. C'est ce qui
  a fait décaler la figure vers la gauche : l'écran du haut n'a pas de bande libre en dessous -
  il s'arrête quatre pixels sous le statut -, donc la place a été prise sur la largeur de la
  colonne de gauche, pas sur la hauteur. La plaque est celle du lieu et du bouton d'appel, accent
  compris : un objet posé seul à côté du dresseur se lisait comme quelque chose qui avait glissé
  de l'équipe.
- **Le rail ne grandit jamais avec la liste.** Il est haut de ce qu'il a de récompenses - une
  seule fait une case, pas une colonne vide avec quelque chose en haut - et plafonné à quatre,
  ce que la bande entre l'équipe et le statut peut tenir. Au-delà, la dernière case compte le
  reste et le nomme au survol, donc quarante récompenses dessinent exactement le même rail que
  quatre. La liste de ce survol est bornée à son tour : une infobulle plus haute que la fenêtre
  ne répond pas mieux que pas d'infobulle.
- **L'infobulle de l'écran est une liste de lignes**, pas une ligne, depuis ce rail. Les autres
  survols - équipe, bouton d'appel - n'en donnent qu'une et la rendent telle quelle.
- **Le compte est celui de vanilla** (`renderItemDecorations`), dans le coin de l'icône, qui
  n'écrit rien pour un objet seul - c'est déjà ce qu'un joueur lit comme « un ». Un « 1x » posé
  à côté de l'icône a existé : il coûtait une colonne de large pour ne rien dire.
- **Elles arrivent en `ItemStack`, pas en ID.** Le serveur les résout avec
  `TrainerRewards.preview`, exactement la résolution qui les remet au vainqueur : une fiche ne
  peut donc pas annoncer une récompense qui ne serait jamais donnée, et le client n'a ni ID à
  résoudre ni compte à borner. `ItemStack.OPTIONAL_STREAM_CODEC` fait le transport.
- **Elles sont visibles avant la victoire**, contrairement à l'équipe, et c'est délibéré : une
  équipe est une récompense pour avoir gagné, une récompense est la raison d'essayer. Un pack qui
  veut garder la surprise pose `hidden` sur la ligne concernée - c'est le seul endroit où
  `TrainerRewards.preview` et `grant` divergent volontairement, et le filtre est côté serveur :
  ce qui n'est pas envoyé ne se lit pas sur le réseau.
- **Le drapeau est par récompense, pas par dresseur.** Cacher tout un lot se fait en marquant
  chaque ligne. Un interrupteur au niveau du dresseur en plus de celui-ci ferait deux réglages
  pour un même résultat, donc deux façons de se contredire.
- **Une récompense unique porte le marqueur de la ligne de statut** : le contour tant qu'elle
  est due, la bille pleine une fois réclamée, et l'objet réclamé passe sous un voile de la
  couleur de la plaque. Ce sont les deux mêmes formes que « battu / pas encore » sur le même
  écran, donc le rail répond à « est-ce que je l'aurai encore » sans légende, et l'infobulle le
  dit en mots pour qui lit la marque comme une décoration.
- **La colonne du marqueur n'existe que si une case dessinée la demande.** Le rail passe de 26 à
  36 pixels et reste centré dans les deux cas. Une colonne de cases vides coûterait aux icônes
  leur place dans la bande.
- **Le rail est centré sur ce qui se voit, pas sur les repères du code.** `REWARD_CENTER_X` tombe
  entre le bord du skin et le premier slot de l'équipe. Centrer sur `TEAM_X` collait le rail au
  dresseur : une case d'équipe porte dix-huit pixels d'air avant son slot, donc son bord gauche
  n'est nulle part sur l'écran. `TEAM_SLOT_INSET` et `TEAM_SLOTS_WIDTH` existent pour ça, et la
  plaque du lieu s'aligne dessus elle aussi.
- **`TEAM_X` reste une constante, `TEAM_TOP` est calculé.** Le partage horizontal a été essayé -
  répartir à parts égales tout ce qui reste à droite de la figure - puis retiré : ça déplaçait
  l'équipe entre un dresseur qui donne quelque chose et un qui ne donne rien, et la colonne était
  déjà bien là où elle était. En hauteur en revanche, un `UPPER_Y + 46` en dur laissait un pixel
  sous la dernière rangée et treize au-dessus de la première : `TEAM_TOP` se lit maintenant sur
  ses deux voisins, la ligne du niveau et la plaque du lieu, ce qui garde les deux bouts égaux.
- **Le capot de `frame.png` avait 20 pixels de bordure à gauche et 13 à droite.** L'écran du haut
  n'était donc pas centré dans son cadre, ce qui se lit tout de suite comme un fond raté du côté
  étroit. Sept colonnes de corps ont été retirées à gauche - le biseau extérieur passe de 31 à 38
  et les deux bordures font 13. Le trou, lui, n'a pas bougé : aucune constante de l'écran ne
  change, et la base en dessous non plus, qui déborde le capot de ce côté de toute façon.
- **Le voile passe par `RenderType.guiOverlay()`**, celui dont vanilla éclaire ses propres
  cases : un `fill` ordinaire se dessine *sous* le modèle d'un objet, donc il ne griserait rien
  du tout.
- **Les objets se dessinent après les aplats et avant les modèles.** `GuiGraphics.renderItem`
  vide le lot en cours et gère lui-même la profondeur, donc il ne doit pas couper une série de
  `fill` ou de blits - même raison que pour les modèles, un cran plus tôt.
- **`const val REWARD_TOP = TEAM_TOP` doit être déclaré après `TEAM_TOP`.** Une constante Kotlin
  ne regarde pas en avant dans le même objet, et l'erreur (« Variable must be initialized »)
  ne dit pas que c'est une question d'ordre.

