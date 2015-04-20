var drawUtilsModule = (function() {
    return {
        cardDisplay: function(card) {
            var format = card["card"] + " $" + card["cost"] + " (" + card["quantity"] + ")";
            return $('<p>', {
                class: "card-display",
                text: format
            });
        }
    }
})();