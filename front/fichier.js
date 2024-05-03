
var div2 = document.getElementById('2');


if (div2) {
    // Sélection de tous les enfants de l'élément avec l'ID "2"
    var div2Children = div2.children;

    for (var i = 0; i < div2Children.length; i++) {
        var child = div2Children[i];

        var imgPB = document.createElement("img");
        imgPB.src = "../img/black_pawn.svg";

    
        child.appendChild(imgPB);
    }
} else {
    console.error("Aucun élément avec l'ID '2' trouvé.");
}
