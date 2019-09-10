var items = [
  {
    id: '001',
    type: 'Violin',
    observations: 'En buen estado'
  },
  {
    id: '002',
    type: 'Piano',
    observations: 'En buen estado'
  },
  {
    id: '003',
    type: 'Violin',
    observations: 'En Reparacion'
  }
]

const types = [
  {name:'violin', value:'violín' },
  {name:'violonchelo', value:'violonchelo'},
  {name:'piano', value:'piano'},
  {name:'flauta', value:'flauta'},
  {name:'trombom', value:'trombón'},
]


function addItem(){
  const table = document.getElementById('tabla')
  const id = document.getElementById('id').value
  const type = document.getElementById('type').value
  const observation = document.getElementById('description').value

  if(!id || items.find(elem => elem.id === id) || !type || observation.length > 255){
    return alert('valores invalidos')
  }
  var row = table.insertRow(items.length+1)
  row.insertCell(0).innerHTML = id
  row.insertCell(1).innerHTML = type
  row.insertCell(2).innerHTML = observation
  items.push({
    id,
    type,
    observation
  })
}

function reset(){
  document.getElementById('id').value = ''
  document.getElementById('type').value = ''
  document.getElementById('description').value = ''
}

function init(){
  const select = document.getElementById('type')
  types.map(type => {
    let option = document.createElement("option")
    option.text = type.name
    select.add(option)

  })
  items.map((item, i) => {
    const table = document.getElementById('tabla')
    var row = table.insertRow(i+1)
    row.insertCell(0).innerHTML = item.id
    row.insertCell(1).innerHTML = item.type
    row.insertCell(2).innerHTML = item.observations
  })
}
