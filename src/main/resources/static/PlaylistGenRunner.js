const playlistGenerator = {
        
    addListeners: function() {
        document.getElementById("generateButton")
        .addEventListener("click", () => {
        document.getElementById("patience").innerHTML = "<h4>Sentence send for generation, it may take a while</h4>"
            this.generatePlaylist()
        })
    },
        
    generatePlaylist: async function () {
        const apiUrl = "http://localhost:9090/playlist/new"
        title = document.getElementById("titleField").value
        sentence = document.getElementById("sentenceField").value
        const response = await fetch(apiUrl, {
            method: "POST",
            body: JSON.stringify({
                "name":title,
                "sentence":sentence
            }),
            headers: {
              "Content-type": "application/json; charset=UTF-8"
            }
        })
        const externalUrls = await response.json()
        document.location.href = externalUrls.playlist
    }

}

playlistGenerator.addListeners()