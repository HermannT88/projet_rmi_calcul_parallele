# Projet RMI : Lancer de rayons distribué (Raytracer)

## Architecture

L'application est découpée en 3 éléments principaux :

1. **Service Central (Répartiteur)** : `LancerDispatcher` instancie le `DispatcherService`. Il gère le découpage de l'image globale en petits blocs (tâches) et la synchronisation finale.
2. **Nœuds de Calcul** : `LancerMultiNoeuds` permet de lancer un ou plusieurs noeuds simultanément (via `ComputeNodeImpl`). Ils demandent les tâches, les calculent en local, et renvoient le résultat RMI.
3. **Le Client** : `ClientRaytracer` lit le fichier contenant la scène (`simple.txt`), demande le calcul complet au serveur RMI, attend le résultat, et l'affiche.

---

## lancer l'application

### 1. Compiler

```powershell
javac -cp . *.java raytracer/*.java
```

### 2. Démarrer l'annuaire

```powershell
rmiregistry 1099
```

### 3. Lancer le Service Central

```powershell
java LancerDispatcher
```

### 4. Lancer les Nœuds de Calcul

```powershell
java LancerMultiNoeuds 4
```

### 5. Lancer le Client

```powershell
java ClientRaytracer simple.txt 896 456
```
