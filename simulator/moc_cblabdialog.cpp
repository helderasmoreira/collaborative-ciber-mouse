/****************************************************************************
** Meta object code from reading C++ file 'cblabdialog.h'
**
** Created: Sat Nov 3 14:40:16 2012
**      by: The Qt Meta Object Compiler version 63 (Qt 4.8.3)
**
** WARNING! All changes made in this file will be lost!
*****************************************************************************/

#include "cblabdialog.h"
#if !defined(Q_MOC_OUTPUT_REVISION)
#error "The header file 'cblabdialog.h' doesn't include <QObject>."
#elif Q_MOC_OUTPUT_REVISION != 63
#error "This file was generated using the moc from 4.8.3. It"
#error "cannot be used with the include files from this version of Qt."
#error "(The moc has changed too much.)"
#endif

QT_BEGIN_MOC_NAMESPACE
static const uint qt_meta_data_cbLabDialog[] = {

 // content:
       6,       // revision
       0,       // classname
       0,    0, // classinfo
       4,   14, // methods
       0,    0, // properties
       0,    0, // enums/sets
       0,    0, // constructors
       0,       // flags
       0,       // signalCount

 // slots: signature, parameters, type, tag, flags
      13,   12,   12,   12, 0x09,
      28,   22,   12,   12, 0x08,
      61,   12,   12,   12, 0x08,
      91,   12,   12,   12, 0x08,

       0        // eod
};

static const char qt_meta_stringdata_cbLabDialog[] = {
    "cbLabDialog\0\0accept()\0index\0"
    "on_treeView_clicked(QModelIndex)\0"
    "on_pushButton_Apply_clicked()\0"
    "restoreDefaults()\0"
};

void cbLabDialog::qt_static_metacall(QObject *_o, QMetaObject::Call _c, int _id, void **_a)
{
    if (_c == QMetaObject::InvokeMetaMethod) {
        Q_ASSERT(staticMetaObject.cast(_o));
        cbLabDialog *_t = static_cast<cbLabDialog *>(_o);
        switch (_id) {
        case 0: _t->accept(); break;
        case 1: _t->on_treeView_clicked((*reinterpret_cast< const QModelIndex(*)>(_a[1]))); break;
        case 2: _t->on_pushButton_Apply_clicked(); break;
        case 3: _t->restoreDefaults(); break;
        default: ;
        }
    }
}

const QMetaObjectExtraData cbLabDialog::staticMetaObjectExtraData = {
    0,  qt_static_metacall 
};

const QMetaObject cbLabDialog::staticMetaObject = {
    { &QDialog::staticMetaObject, qt_meta_stringdata_cbLabDialog,
      qt_meta_data_cbLabDialog, &staticMetaObjectExtraData }
};

#ifdef Q_NO_DATA_RELOCATION
const QMetaObject &cbLabDialog::getStaticMetaObject() { return staticMetaObject; }
#endif //Q_NO_DATA_RELOCATION

const QMetaObject *cbLabDialog::metaObject() const
{
    return QObject::d_ptr->metaObject ? QObject::d_ptr->metaObject : &staticMetaObject;
}

void *cbLabDialog::qt_metacast(const char *_clname)
{
    if (!_clname) return 0;
    if (!strcmp(_clname, qt_meta_stringdata_cbLabDialog))
        return static_cast<void*>(const_cast< cbLabDialog*>(this));
    return QDialog::qt_metacast(_clname);
}

int cbLabDialog::qt_metacall(QMetaObject::Call _c, int _id, void **_a)
{
    _id = QDialog::qt_metacall(_c, _id, _a);
    if (_id < 0)
        return _id;
    if (_c == QMetaObject::InvokeMetaMethod) {
        if (_id < 4)
            qt_static_metacall(this, _c, _id, _a);
        _id -= 4;
    }
    return _id;
}
QT_END_MOC_NAMESPACE
