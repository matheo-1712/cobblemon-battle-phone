# Cobblemon BattlePhone

Le **Battle Phone** des dresseurs de
[Cobblemon Trainers](https://github.com/matheo-1712/cobblemon-trainers) : un objet qui liste les
dresseurs du monde, ce qu'ils rapportent, ceux qui sont déjà vaincus, et qui appelle ceux qui
déclarent un lieu.

Ce mod est construit sur Cobblemon Trainers et ne fait rien sans lui : les dresseurs, leurs
victoires, leurs conditions et leurs récompenses viennent de là. Lui ne fournit que l'objet,
son écran et ce qui l'alimente.

## Prérequis

| | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| Fabric Loader ou NeoForge | Fabric ≥ 0.19.5 ; NeoForge ≥ 21.1.251 |
| Java | 21 |
| Cobblemon | ≥ 1.8.0 |
| Cobblemon Trainers | ≥ 1.1.4 |
| Fabric API et Fabric Language Kotlin | requis avec Fabric |
| Kotlin for Forge | requis avec NeoForge (≥ 5.12.0) |

**Requis des deux côtés en multijoueur**, comme Cobblemon Trainers : l'écran n'existe que chez
un client qui a le mod.

## Le téléphone

`/cobblemontrainers list` est réservé aux opérateurs. Le **Battle Phone** est la même chose
pour tout le monde : un clic droit et l'écran liste les dresseurs du monde, le skin de chacun,
et dit lesquels sont déjà vaincus.

Les dresseurs sont **triés par datapack** : les flèches en haut de la liste (ou les touches
gauche et droite) passent d'un pack à l'autre, avec un onglet « tous les datapacks » en
premier dès qu'il y en a plusieurs - celui-là sépare les packs par un intertitre. À
l'intérieur d'un pack, un intertitre par **catégorie** - le dossier où le pack range ses
dresseurs -, chacun avec son propre compteur.

Cliquer une ligne montre le dresseur en entier à droite, avec son niveau, la taille de son
équipe et son état - vaincu, vaincu sans revanche possible, encore debout, ou fermé tant que
ses conditions ne sont pas remplies, auquel cas la liste de ce qui manque remplace son
équipe. À côté de lui, ce que le battre rapporte : une case par objet, avec sa quantité, et
au-delà de quatre la dernière case compte le reste et le nomme au survol. Contrairement à son
équipe, les récompenses sont visibles avant de l'avoir battu, et un pack peut en garder une
secrète avec `"hidden": true`. Une récompense qui ne tombe qu'à la première victoire porte un
marqueur, et son icône est grisée une fois réclamée. Six cases pour son équipe restent fermées
jusqu'à la victoire, puis affichent les modèles de ses Pokémon, formes et chromatiques
comprises.

L'objet ne retient rien : il lit la progression du serveur, donc deux exemplaires affichent la
même chose et le perdre ne perd rien.

Seuls les dresseurs en `"listed": true` y apparaissent, et un dresseur verrouillé peut se
cacher entièrement jusqu'à ce que ses conditions soient remplies - voir le
[format des dresseurs](https://github.com/matheo-1712/cobblemon-trainers/blob/master/docs/DATAPACK.md#le-suivi-de-progression).

### Le craft

Quatre lingots de fer aux coins, quatre lingots de cuivre sur les côtés, et une **noigrume au
choix** au centre. Sa couleur donne celle du téléphone : noir, bleu, vert, rose, rouge, blanc
ou jaune. Toutes les couleurs ouvrent les mêmes dresseurs ; le cadre et les couleurs de
l'interface suivent celles du téléphone.

<p align="center"><img src="https://raw.githubusercontent.com/matheo-1712/cobblemon-trainers/refs/heads/master/assets/battle_phone_craft.png" alt="Battle Phone recipe"></p>

Il se trouve aussi dans l'onglet créatif **Cobblemon Trainers**, ou s'obtient avec :

```
/give @s cobblemon-trainers:battle_phone
```

Pour une autre couleur : `battle_phone_black`, `battle_phone_green`, `battle_phone_pink`,
`battle_phone_red`, `battle_phone_white` ou `battle_phone_yellow`, toujours après
`cobblemon-trainers:`. **Les objets gardent l'ID qu'ils avaient** quand le téléphone faisait
partie de Cobblemon Trainers : les téléphones déjà dans un monde le restent.

### Appeler un dresseur

Un dresseur qui déclare **où il se trouve** est appelable depuis sa fiche : le téléphone affiche
son lieu, et un bouton **Appeler** le fait venir à quelques dizaines de blocs quand le joueur y
est. Un dresseur qui ne déclare rien n'a pas de bouton et se trouve à pied. Le lieu se déclare
dans le dresseur - voir
[SPAWNING.md](https://github.com/matheo-1712/cobblemon-trainers/blob/master/docs/SPAWNING.md).

## Développement

Le mod compile contre les jars publiés de Cobblemon Trainers. Tant qu'ils ne sont pas sur un
Maven public, ils se prennent en Maven local :

```bash
# dans le dépôt cobblemon-trainers
./gradlew :fabric:publishToMavenLocal :neoforge:publishToMavenLocal
```

```bash
# ici
./gradlew build
```

La version attendue est `cobblemon_trainers_version` dans `gradle.properties`. Sur Windows,
utiliser `.\gradlew.bat`.

## Licence

CC0-1.0, comme Cobblemon Trainers.
