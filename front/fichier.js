
var div2 = document.getElementById('2');


if (div2) {
    // Sélection de tous les enfants de l'élément avec l'ID "2"
    var div2Children = div2.children;

    for (var i = 0; i < div2Children.length; i++) {
        var child = div2Children[i];

        var imgPB = document.createElement("img");
        imgPB.src = "echiquier/public/image/black_pawn.svg";

    
        child.appendChild(imgPB);
    }
} else {
    console.error("Aucun élément avec l'ID '2' trouvé.");
}


var div7 = document.getElementById('7');


if (div7) {
    // Sélection de tous les enfants de l'élément avec l'ID "7"
    var div7Children = div7.children;

    for (var i = 0; i < div7Children.length; i++) {
        var childs = div7Children[i];

        var imgPBs = document.createElement("img");
        imgPBs.src = "img/white_pawn.svg";

    
        childs.appendChild(imgPBs);
    }
} else {
    console.error("Aucun élément avec l'ID '2' trouvé.");
}
