requirejs.config({
    baseUrl: 'lib',

    paths: {
        modules: '../modules/'
    }
});

define(['modules/dependances', 'modules/arbreUtils'], function (dependances, arbreUtils) {

    // création de l'arbre des packages / artefact
    var racineArbre = document.getElementById("racineArbre");
    var arbre = arbreUtils.creerArbre(racineArbre);

    // recherche des premiers enfants 
    dependances.chercherEnfants('', premieresRacinesCallback);

    // callback de la recherche des premiers enfants 
    function premieresRacinesCallback(donnees) {
        var optionsRacine, i;

        for (i = 0; i < donnees.length; i++) {
            optionsRacine = {
                id: donnees[i].name,
                label: donnees[i].name,
                dernier: (i === donnees.length-1)
            }
            arbre.ajouterNoeud(optionsRacine);           
        }
    }

    // création de l'arbre des dépendances des artefacts : 
    var racineArbreDep = document.getElementById("racineArbreDep");
    var arbreDep = arbreUtils.creerArbre(racineArbreDep);
    var racine = {
        id: "dependances",
        label: "Dépendances"
    };
    arbreDep.ajouterNoeud(racine);

    // création de l'arbre des dépendances inversées des artefacts : 
    var racineArbreDepInv = document.getElementById("racineArbreDepInv");
    var arbreDepInv = arbreUtils.creerArbre(racineArbreDepInv);
    var racine = {
        id: "dependancesInv",
        label: "Dépendances inversées"
    };
    arbreDep.ajouterNoeud(racine);

    /**
     * Gestionnaire d'évènement d'activation d'un noeud de l'arbre 
     */
    arbre.noeudHandler = function (noeud) {
        var self = noeud;
        dependances.chercherEnfants(noeud.id, function (enfants) {
            var enfant, options = {};
            //console.log("Appel du callback chercherEnfants");

            for (var i = 0; i < enfants.length; i++) {
                enfant = enfants[i];
                options.parent = self;
                if (i == enfants.length - 1) {
                    options.dernier = true;
                } else {
                    options.dernier = false;
                }
                if (!enfant.id) {
                    options.id = arbreUtils.genererIdEnfant(self.id, enfant.name);
                    options.label = arbreUtils.genererLabelEnfant(self.id, enfant.name);
                    self.arbre.ajouterNoeud(options);
                } else {
                    options.label = arbreUtils.genererLabelEnfant(self.id, enfant.id);
                    options.id = enfant.id;
                    options.active = true;
                    self.arbre.ajouterFeuille(options);
                }
                //console.log("Ajout d'un enfant " + options.label + " au parent " + options.parent.id);
            }
        });
    }

    /**
     * Mise à jour du détail d'un artefact dans la vue  
     */
    function afficherDetail(donnees) {
        var valueId = document.getElementById("id");
        valueId.innerHTML = donnees.id;

        var valueGroupId = document.getElementById("groupId");
        valueGroupId.innerHTML = donnees.designation.groupId;

        var valueArtifactId = document.getElementById("artifactId");
        valueArtifactId.innerHTML = donnees.designation.artifactId;

        var valueVersion = document.getElementById("version");
        valueVersion.innerHTML = donnees.designation.version;

        document.getElementById("detailDependances").style.visibility = "visible";
    }

    /**
     * Gestionnaire d'évènement d'activation d'une feuille de l'arbre 
     */
    arbre.feuilleHandler = function (feuille) {
        var self = feuille;

        function detailCallback(donnees) {
            var i, dep;
            afficherDetail(donnees);
            arbreDep.supprimerNoeuds();

            for (i = 0; i < donnees.dependencies.length; i++) {
                dep = donnees.dependencies[i];
                arbreDep.ajouterFeuille({ label: arbreUtils.genererLabelDependence(dep), active: false });
            }
        }

        function dependencesInverseesCallback(donnees) {
            var i, dep;
            arbreDepInv.supprimerNoeuds();

            for (i = 0; i < donnees.length; i++) {
                dep = donnees[i];
                arbreDepInv.ajouterFeuille({ label: arbreUtils.genererLabelDependence(dep), active: false });
            }
        }
        dependances.chercherDetail(self.id, detailCallback, dependencesInverseesCallback);
    }

});
