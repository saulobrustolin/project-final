db = db.getSiblingDB('final-project');

db.Users.insert({
    name: 'João Gomes',
    email: 'joaogomes@gmail.com',
    cpf: '302.408.410-00',
    balance: 0,
    password: '$2a$10$kcvWJuj9ySzuR9vYM0P6c.pV0LgO.geJKYc33CzqssYo4/iQkRsqu',
    role: 'USER',
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    _class: 'saulo.brustolin.project.entities.User'
});