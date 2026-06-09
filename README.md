# Projet RMI : Lancer de rayons distribué (Raytracer)

## Architecture

L'application est découpée en 3 éléments principaux :

1. **Service Central** : `LancerService` instancie le `ServiceCentral`. Il maintient uniquement la liste des nœuds de calcul disponibles. Il ne distribue pas le travail lui-même.
2. **Nœuds de Calcul** : `LancerMultiNoeuds` lance un ou plusieurs nœuds simultanément (via `ComputeNodeImpl`). Chaque nœud s'enregistre auprès du service central et attend qu'on lui envoie des blocs à calculer.
3. **Le Client** : `ClientRaytracer` récupère la liste des nœuds auprès du service central, découpe l'image en blocs (grille 10×10), envoie chaque bloc à un nœud en parallèle (un thread par nœud), puis assemble l'image finale.

---

## Structure des packages

```
calcul_parallele/
├── client/
│   ├── ClientRaytracer.java   # Client principal
│   ├── LancerService.java     # Lance le service central
│   └── Task.java              # Représente un bloc à calculer
├── noeud_calcul/
│   ├── ComputeNode.java       # Interface RMI des nœuds
│   ├── ComputeNodeImpl.java   # Implémentation du nœud
│   └── LancerMultiNoeuds.java # Lance N nœuds en parallèle
├── service/
│   ├── ServiceInterface.java  # Interface RMI du service central
│   └── ServiceCentral.java    # Implémentation du service central
└── raytracer/                 # Moteur de rendu (fourni)
```

---

## Lancer l'application

### 1. Compiler

```powershell
javac -cp calcul_parallele calcul_parallele/client/*.java calcul_parallele/noeud_calcul/*.java calcul_parallele/service/*.java calcul_parallele/raytracer/*.java
```

### 2. Lancer le Service Central

```powershell
java -cp calcul_parallele service.LancerService
```

### 3. Lancer les Nœuds de Calcul

```powershell
java -cp calcul_parallele noeud_calcul.LancerMultiNoeuds 4
```

### 4. Lancer le Client

```powershell
java -cp calcul_parallele client.ClientRaytracer simple.txt 896 456
```

```powershell
java -cp calcul_parallele client.ClientRaytracer simple.txt 896 456 192.168.1.10
```
