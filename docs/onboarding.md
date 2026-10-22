# Visite guidée du premier lancement

Au tout premier lancement (et à chaque fois que le flag est réinitialisé), l'application ouvre une
visite guidée avant l'écran de permission GPS : elle explique Position, Naviguer, Satellites
(skyplot + globe 3D), le Carnet et les Succès directement dans les **écrans réels** de
l'application, superposés d'un voile, d'un halo pulsant sur le bouton concerné et d'une bulle
d'explication.

## Pourquoi un conteneur séparé

L'onboarding rejoue les vrais écrans (`ui/navigation/MainScaffold`), mais au premier lancement il
n'y a aucune donnée réelle — aucun fix GPS, aucune session, aucun succès. Il utilise un conteneur
isolé avec une position et un historique fictifs, uniquement le temps de la visite guidée.

L'onboarding construit donc son propre graphe de dépendances (`ui/onboarding/OnboardingContainer`,
qui implémente `data/AppContainer` — la même interface que `CairnApplication`) :

- `LocationRepository.simulated(context)` / `CompassRepository.simulated(context)` : des générateurs
  de position et de cap injectés uniquement dans le conteneur temporaire de la visite.
- Une base Room **en mémoire** (`AppDatabase.inMemory`), remplie une fois par
  `demo/DemoDataSeeder`. La vraie `cairn.db` n'est jamais ouverte.
- `SettingsRepository` est le vrai : la visite respecte le thème et le format de coordonnées déjà
  choisis par l'utilisateur.
- `GamificationManager` tourne sur ce graphe mais sans jamais appeler `startLiveTracking()` : son
  `init` suffit à dériver records et succès des sessions semées.

Résultat : `MainScaffold` ne sait pas si on lui passe `CairnApplication` ou
`OnboardingContainer` — les écrans sont strictement identiques, seules les données changent.

## Comment une étape désigne un bouton

Chaque élément que la visite peut mettre en évidence est marqué dans son écran réel par
`Modifier.onboardingTarget(OnboardingTarget.XXX)` (`ui/onboarding/OnboardingTarget.kt`). Ce
modifier ne fait rien tant qu'il n'existe pas d'`OnboardingTargets` fourni par un
`CompositionLocal` — donc coût nul et aucun changement de comportement dans l'app normale. Pendant
la visite, `OnboardingHost` fournit ce `CompositionLocal` et lit les rectangles rapportés pour
positionner la découpe du voile.

## Le scénario

`ui/onboarding/OnboardingStep.kt` liste les 16 étapes (écran, cible, texte). `OnboardingHost`
pilote son propre `NavHostController` (distinct de celui de l'app réelle) pour amener l'écran
affiché sur la route de chaque étape.

## Premier lancement et rejeu

Le flag `onboarding_completed` (DataStore, `SettingsRepository`) vaut `false` par défaut : tout le
monde — nouvelle installation comme mise à jour — voit la visite une fois.
`MainActivity` bascule entre `OnboardingHost` et l'app réelle selon ce flag. Il est
volontairement **hors de `AppSettings`** : ce modèle est sérialisé entier dans une sauvegarde, et
restaurer une sauvegarde ne doit jamais rejouer — ni escamoter — la visite sur un autre appareil.

Rejouable depuis **Réglages → Aide → Rejouer la visite**, qui appelle simplement
`SettingsViewModel.replayOnboarding()` (remet le flag à `false`).
